    package com.elocate.elocate.service;

import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.User;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CertificateService certificateService;

    // Output folder — same as the certificate-templates resources folder
    private static final Path OUTPUT_DIR = Paths.get(
            "src/main/resources/certificate-templates"
    );

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(certificateService, "appBaseUrl", "https://elocate.app");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private RecycleRequest buildRequest() {
        DeviceModel device = DeviceModel.builder()
                .id(UUID.randomUUID())
                .modelName("iPhone 14 Pro")
                .build();

        return RecycleRequest.builder()
                .id(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .requestNumber("REQ-2024-042")
                .userId(UUID.randomUUID())
                .deviceModel(device)
                .build();
    }

    private User buildUser() {
        User user = new User();
        user.setFullName("Arjun Sharma");
        return user;
    }

    // ── Test 1: HTML is generated with correct placeholders replaced ──────────

    @Test
    void testBuildCertificateHtml_replacesAllPlaceholders() {
        RecycleRequest request = buildRequest();
        User citizen = buildUser();

        String html = certificateService.buildCertificateHtml(request, citizen);

        assertThat(html).contains("Arjun Sharma");
        assertThat(html).contains("iPhone 14 Pro");
        assertThat(html).contains("REQ-2024-042");
        assertThat(html).contains("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(html).contains("https://elocate.app");
        assertThat(html).doesNotContain("{{citizenName}}");
        assertThat(html).doesNotContain("{{deviceName}}");
        assertThat(html).doesNotContain("{{requestNumber}}");
        assertThat(html).doesNotContain("{{requestId}}");
        assertThat(html).doesNotContain("{{certificateDate}}");
        assertThat(html).doesNotContain("{{appBaseUrl}}");

        System.out.println("✅ HTML placeholders replaced correctly");
    }

    // ── Test 2: Certificate uploaded to S3, returns URL ──────────────────────

    @Test
    void testGenerateAndUploadCertificate_uploadsToS3AndReturnsUrl() {
        RecycleRequest request = buildRequest();
        User citizen = buildUser();

        String expectedUrl = "https://elocate-bucket.s3.ap-south-1.amazonaws.com/certificates/a1b2c3d4-e5f6-7890-abcd-ef1234567890.html";
        when(s3Service.getPublicUrl(anyString())).thenReturn(expectedUrl);

        String resultUrl = certificateService.generateAndUploadCertificate(request, citizen);

        // Verify S3 upload was called with correct key and content type
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<Long> lengthCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);

        verify(s3Service).uploadFile(
                keyCaptor.capture(),
                streamCaptor.capture(),
                lengthCaptor.capture(),
                contentTypeCaptor.capture()
        );

        assertThat(keyCaptor.getValue()).isEqualTo("certificates/a1b2c3d4-e5f6-7890-abcd-ef1234567890.html");
        assertThat(contentTypeCaptor.getValue()).isEqualTo("text/html");
        assertThat(lengthCaptor.getValue()).isGreaterThan(0);
        assertThat(resultUrl).isEqualTo(expectedUrl);

        System.out.println("✅ S3 upload verified — key: " + keyCaptor.getValue());
        System.out.println("✅ Returned URL: " + resultUrl);
    }

    // ── Test 3: Convert HTML to PDF and save to certificate-templates folder ──

    @Test
    void testGeneratePdf_savesToCertificateTemplatesFolder() throws Exception {
        RecycleRequest request = buildRequest();
        User citizen = buildUser();

        // Use the same HTML template — Playwright renders it identically to the browser
        String html = certificateService.buildCertificateHtml(request, citizen);
        byte[] pdfBytes = convertHtmlToPdf(html);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(1000); // valid PDF is never tiny

        // Save to src/main/resources/certificate-templates/
        Files.createDirectories(OUTPUT_DIR);
        Path outputPath = OUTPUT_DIR.resolve("certificate_TEST_" +
                request.getId().toString().replace("-", "").substring(0, 8).toUpperCase() + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            fos.write(pdfBytes);
        }

        assertThat(outputPath.toFile()).exists();
        assertThat(outputPath.toFile().length()).isGreaterThan(0);

        System.out.println("✅ PDF saved to: " + outputPath.toAbsolutePath());
    }

    // ── Test 4: Fallback — null name/device uses defaults ────────────────────

    @Test
    void testBuildCertificateHtml_usesDefaultsWhenFieldsAreNull() {
        RecycleRequest request = RecycleRequest.builder()
                .id(UUID.randomUUID())
                .requestNumber(null)
                .userId(UUID.randomUUID())
                .deviceModel(null)
                .build();

        User citizen = new User(); // fullName is null

        String html = certificateService.buildCertificateHtml(request, citizen);

        assertThat(html).contains("Valued Citizen");
        assertThat(html).contains("Electronic Device");
        assertThat(html).doesNotContain("{{citizenName}}");
        assertThat(html).doesNotContain("{{deviceName}}");

        System.out.println("✅ Defaults applied for null fields");
    }

    // ── PDF conversion helper (uses Playwright headless Chromium) ─────────────

    private byte[] convertHtmlToPdf(String html) {
        final int WIDTH_PX  = 1123;
        final int HEIGHT_PX = 794;

        String pdfHtml = html.replace("</style>",
                """
                /* ── PDF overrides ── */
                @media print { * { -webkit-print-color-adjust: exact; print-color-adjust: exact; } }
                html, body {
                  width: %dpx !important;
                  height: %dpx !important;
                  min-height: unset !important;
                  padding: 0 !important;
                  margin: 0 !important;
                  overflow: hidden !important;
                }
                .frame-outer {
                  width: 100%% !important;
                  max-width: 100%% !important;
                  height: 100%% !important;
                  border-radius: 0 !important;
                  box-shadow: none !important;
                  margin: 0 !important;
                }
                .frame-inner {
                  height: 100%% !important;
                  padding: 28px 44px 22px !important;
                  overflow: hidden !important;
                }
                </style>
                """.formatted(WIDTH_PX, HEIGHT_PX));

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(WIDTH_PX, HEIGHT_PX));
            Page page = ctx.newPage();
            page.setContent(pdfHtml, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE));

            byte[] pdf = page.pdf(new Page.PdfOptions()
                    .setWidth(WIDTH_PX + "px")
                    .setHeight(HEIGHT_PX + "px")
                    .setPrintBackground(true)
                    .setMargin(new com.microsoft.playwright.options.Margin()
                            .setTop("0").setBottom("0").setLeft("0").setRight("0"))
            );
            browser.close();
            return pdf;
        }
    }
}

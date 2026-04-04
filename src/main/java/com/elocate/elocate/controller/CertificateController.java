package com.elocate.elocate.controller;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.User;
import com.elocate.elocate.repository.RecycleRequestRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.service.CertificateService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Media;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;
    private final RecycleRequestRepository recycleRequestRepository;
    private final UserRepository userRepository;

    /**
     * View certificate as HTML in browser.
     * GET /api/v1/certificates/{requestId}
     */
    @GetMapping(value = "/{requestId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> viewCertificate(@PathVariable UUID requestId) {
        String html = getFilledHtml(requestId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Download certificate as PDF — rendered by headless Chromium via Playwright.
     * GET /api/v1/certificates/{requestId}/download
     */
    @GetMapping("/{requestId}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable UUID requestId) {
        String html = getFilledHtml(requestId);   // same beautiful HTML template
        byte[] pdf  = convertHtmlToPdf(html);

        String filename = "elocate-certificate-" + requestId.toString().substring(0, 8).toUpperCase() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String getFilledHtml(UUID requestId) {
        RecycleRequest request = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
        User citizen = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));
        return certificateService.buildCertificateHtml(request, citizen);
    }

    /**
     * Uses Playwright (headless Chromium) to render HTML → PDF.
     * Viewport is set to A4 landscape px dimensions so content fits exactly one page.
     */
    private byte[] convertHtmlToPdf(String html) {
        // A4 landscape at 96dpi: 297mm × 210mm = 1123 × 794 px
        final int WIDTH_PX  = 1123;
        final int HEIGHT_PX = 794;

        // Inject overrides: remove body padding/min-height, lock frame to exact page size
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
            log.info("PDF generated via Playwright, size: {} bytes", pdf.length);
            return pdf;
        } catch (Exception e) {
            log.error("Playwright PDF generation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}

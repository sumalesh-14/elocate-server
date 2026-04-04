"""
generate_certificate.py
Fills the recycling-certificate.html template and saves a PDF in the same folder.

Requirements:
    pip install reportlab xhtml2pdf

Usage:
    python src\main\resources\certificate-templates\generate_certificate.py --name "Arjun Sharma" --device "iPhone 12" --request-number "REQ-2024-042" --request-id "a1b2c3d4-e5f6-7890-abcd-ef1234567890" --base-url "https://elocate.app"
"""

import argparse
from datetime import datetime
from pathlib import Path

try:
    from xhtml2pdf import pisa
except ImportError:
    raise SystemExit("xhtml2pdf is not installed. Run: pip install reportlab xhtml2pdf")


TEMPLATE_FILE = Path(__file__).parent / "recycling-certificate.html"


def generate_certificate(
    citizen_name: str,
    device_name: str,
    request_number: str,
    request_id: str,
    app_base_url: str,
    certificate_date: str | None = None,
    output_file: Path | None = None,
) -> Path:
    if not TEMPLATE_FILE.exists():
        raise FileNotFoundError(f"Template not found: {TEMPLATE_FILE}")

    template = TEMPLATE_FILE.read_text(encoding="utf-8")
    date_str = certificate_date or datetime.now().strftime("%B %d, %Y")

    # xhtml2pdf does not support Google Fonts (requires internet + pango),
    # so swap to safe system fonts for PDF rendering
    filled_html = (
        template
        .replace("{{citizenName}}", citizen_name)
        .replace("{{deviceName}}", device_name)
        .replace("{{requestNumber}}", request_number)
        .replace("{{requestId}}", request_id)
        .replace("{{certificateDate}}", date_str)
        .replace("{{appBaseUrl}}", app_base_url)
        # replace Google Fonts import with system-safe fonts
        .replace(
            "https://fonts.googleapis.com/css2?family=Cinzel:wght@400;600;700&family=EB+Garamond:ital,wght@0,400;0,500;1,400&family=Montserrat:wght@400;500;600&display=swap",
            ""
        )
        .replace("font-family: 'Cinzel', serif;",     "font-family: Georgia, serif;")
        .replace("font-family: 'EB Garamond', serif;","font-family: Georgia, serif;")
        .replace("font-family: 'EB Garamond', Georgia, serif;", "font-family: Georgia, serif;")
        .replace("font-family: 'Montserrat', sans-serif;", "font-family: Arial, sans-serif;")
    )

    if output_file is None:
        safe_id = request_id.replace("-", "")[:8].upper()
        output_file = TEMPLATE_FILE.parent / f"certificate_{safe_id}.pdf"

    with open(output_file, "wb") as pdf_file:
        result = pisa.CreatePDF(filled_html, dest=pdf_file)

    if result.err:
        raise RuntimeError(f"PDF generation failed with {result.err} error(s)")

    print(f"Certificate saved to: {output_file.resolve()}")
    return output_file


def main():
    parser = argparse.ArgumentParser(description="Generate an ELocate recycling certificate as PDF.")
    parser.add_argument("--name",           default="Valued Citizen",                      help="Recipient full name")
    parser.add_argument("--device",         default="Electronic Device",                    help="Device model recycled")
    parser.add_argument("--request-number", default="REQ-0000001",                          help="Human-readable request number")
    parser.add_argument("--request-id",     default="00000000-0000-0000-0000-000000000001", help="UUID of the recycle request")
    parser.add_argument("--base-url",       default="http://localhost:3000",                help="App base URL for verify link")
    parser.add_argument("--date",           default=None,                                   help="Certificate date (default: today)")
    parser.add_argument("--output",         default=None,                                   help="Output PDF path (optional)")
    args = parser.parse_args()

    generate_certificate(
        citizen_name=args.name,
        device_name=args.device,
        request_number=args.request_number,
        request_id=args.request_id,
        app_base_url=args.base_url,
        certificate_date=args.date,
        output_file=Path(args.output) if args.output else None,
    )


if __name__ == "__main__":
    main()

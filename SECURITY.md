# Security Policy

## Reporting a vulnerability

Please report security issues privately through GitHub's **Report a vulnerability** feature rather than opening a public issue with exploit details.

## Root-access boundary

Power Saver requires an explicit Superuser grant and can change protected Wi-Fi, Bluetooth, and power settings. Its launcher activity is exported only so Android can launch it. The activity does not consume intent extras, command strings, file paths, URLs, or other caller-controlled shell input.

Only install APKs from the repository's GitHub Releases page or build from reviewed source. Verify the published SHA-256 checksum before installation.

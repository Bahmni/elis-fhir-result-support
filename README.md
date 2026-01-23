# ELIS FHIR Result Support

Module for creating FHIR diagnostic reports from ELIS lab results in Bahmni.

## Overview

This OpenMRS module processes lab results from ELIS and creates FHIR-compliant DiagnosticReport resources. It integrates with Bahmni's atom feed client to handle lab result events.

## Requirements

- OpenMRS 2.5.12+
- Java 8
- Maven 3.x

## Build

```bash
./mvnw clean install
```

## Features

- Processes lab result encounters from ELIS
- Extracts observations and attachments from orders
- Creates FHIR DiagnosticReport resources
- Updates order fulfiller status
- Handles result status mapping (PRELIMINARY/FINAL)

## License

[Mozilla Public License Version 2.0](LICENSE)

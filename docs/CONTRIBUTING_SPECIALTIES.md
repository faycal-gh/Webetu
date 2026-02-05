# Contributing to University Specialties

The recommendation engine depends on accurate data about universities, fields, and available specialties. This guide explains how to add or update this data.

## Data Structure Overview

All academic data is stored in `backend/src/main/resources/data/academic-structure.json`.
This file is a JSON object mapping university codes to their specific academic structures.

### Hierarchy
1. **University** (e.g., "USTHB", "Oran1")
   - **Fields** (e.g., "Sciences et Technologies")
     - **Levels** (L1, L2, L3)
       - **Options** (Majors/Specialities suitable for the *next* level)

## How to Add a New University

1.  Open `backend/src/main/resources/data/academic-structure.json`.
2.  Add a new key under `universities` with your university code (e.g., `UMBB`).
3.  Define the fields and their structure.

### Example JSON Snippet

```json
"USTHB": {
  "name": "University Of Science And Technology Houari Boumediene",
  "nameAr": "جامعة هواري بومدين للعلوم والتكنولوجيا",
  "fields": [
    {
      "code": "ST",
      "name": "Sciences et Technologies",
      "levels": {
        "L1": {
          "name": "1ère année",
          "nextOptions": ["L2_GM", "L2_ELT"] 
        }
      }
    }
  ]
}
```

## How to Add a New Specialty

To add a specialty (e.g., a new Master's option for L3 students):

1.  Locate the relevant **Field** and **Level**.
2.  Add the specialty to the `specialities` (for L3) or `majors` (for L2) list.
3.  **Required Fields**:
    *   `code`: Unique identifier (e.g., `M_IL`).
    *   `name`: French name.
    *   `nameAr`: Arabic name.
    *   `parentMajor`: The code of the L2 major that leads to this.

### Example: Adding an AI Master

```json
{
  "code": "M_IA",
  "name": "Intelligence Artificielle",
  "nameAr": "ذكاء اصطناعي",
  "parentSpeciality": "L3_SI" // This means students from L3 SI can do this master
}
```

## Recommendation Logic

The AI recommendation engine uses this data to:
1.  Identify what a student is *currently* studying.
2.  Look up valid *next steps* in this JSON file.
3.  Filter options based on the student's current path (e.g., an L2 Mechanical Engineering student will only see L3 Mechanical options).

By keeping this file accurate, the AI can give valid, university-specific advice.

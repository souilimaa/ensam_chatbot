package com.ensam.chatbot.llm;

public class PromptFactory {

    public static String intentPrompt(String question) {
        return """
You are an intent extractor.
Return ONLY valid JSON. No text, no markdown.

Allowed intents:
PERSON_PFE, PERSON_MAJOR, PERSON_SKILLS, PERSON_CURRENT_JOB, ANALYTICS_TOP, UNKNOWN

Rules (VERY IMPORTANT):
- Use PERSON_* only if a specific person's full name is present in the question.
- If the question talks about promo/year/major WITHOUT a person name => use ANALYTICS_TOP.
- "where promo YYYY worked", "where are students working" => metric = CURRENT_EMPLOYERS.
- If metric is unclear for analytics, choose CURRENT_EMPLOYERS.

🎓 Normalized majors (majorNorm) — use ONLY these values:

BIG_DATA_IOT:
- big data
- data engineering
- data analytics
- iot
- internet of things
- big data and iot
- big data & iot
- data & iot
- data iot

MECHATRONIQUE:
- mecatronique, mécatronique
- mechatronic, mechatronics

GENIE_ELECTRIQUE:
- electrique, électrique
- electrical

GENIE_ELECTROMECANIQUE:
- electromecanique, électromécanique
- electromechanical

GENIE_MECANIQUE:
- mecanique, mécanique
- mechanical

GENIE_INDUSTRIEL:
- industriel
- industrial

INFORMATIQUE:
- informatique
- computer science

AUTOMATIQUE:
- automatique
- automation
- control

For ANALYTICS_TOP:
- metric must be one of: PFE_COMPANIES, CURRENT_EMPLOYERS, JOB_TITLES, SKILLS
- filters may include:
  • promoYear (number)
  • majorNorm (ONE of the normalized majors above)
- topK optional (default 10)

JSON schema:
{
  "intent": "...",
  "personName": null or string,
  "promoYear": null or number,
  "majorNorm": null or string,
  "metrics": [] or ["SKILLS","PFE_COMPANIES",...],
  "topK": null or number
}

Rule:
- If question asks for multiple things (skills + pfe, employers + titles, etc.), include multiple metrics in "metrics".
Examples:

Q: "skills of promo 2025 of big data and iot and where they passed their pfe"
A: {"intent":"ANALYTICS_TOP","personName":null,"promoYear":2025,"majorNorm":"BIG_DATA_IOT","metrics":["SKILLS","PFE_COMPANIES"],"topK":10}

Q: "Skills of promo 2025 of big data and iot"
A: {"intent":"ANALYTICS_TOP","personName":null,"promoYear":2025,"majorNorm":"BIG_DATA_IOT","metrics":["SKILLS"],"topK":10}

Q: "Top PFE companies for promo 2022"
A: {"intent":"ANALYTICS_TOP","personName":null,"promoYear":2022,"majorNorm":null,"metrics":["PFE_COMPANIES"],"topK":10}

Q: "Where promo 2022 worked at?"
A: {"intent":"ANALYTICS_TOP","personName":null,"promoYear":2022,"majorNorm":null,"metrics":["CURRENT_EMPLOYERS"],"topK":10}

Question: "%s"
""".formatted((question == null ? "" : question).replace("\"", "\\\""));
    }
}

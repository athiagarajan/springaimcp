package com.springaimcp.service;

import com.springaimcp.model.Temple;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class TempleTranslationFallback {

    private static final List<Map.Entry<String, String>> VOCAB_TA = new ArrayList<>();
    private static final List<Map.Entry<String, String>> VOCAB_TE = new ArrayList<>();
    private static final List<Map.Entry<String, String>> VOCAB_HI = new ArrayList<>();

    private static void addVocab(String en, String ta, String te, String hi) {
        VOCAB_TA.add(Map.entry(en, ta));
        VOCAB_TE.add(Map.entry(en, te));
        VOCAB_HI.add(Map.entry(en, hi));
    }

    static {
        // Titles & Deities
        addVocab("sri", "ஸ்ரீ", "శ్రీ", "श्री");
        addVocab("shri", "ஸ்ரீ", "శ్రీ", "श्री");
        addVocab("temple", "திருக்கோயில்", "ఆలయం", "मंदिर");
        addVocab("kovil", "கோயில்", "కోవిల్", "मंदिर");
        addVocab("shrine", "திருக்கோயில்", "ఆలయం", "मंदिर");
        addVocab("lord", "இறைவன்", "భగవంతుడు", "भगवान");
        addVocab("shiva", "சிவபெருமான்", "శివుడు", "भगवान शिव");
        addVocab("siva", "சிவபெருமான்", "శివుడు", "भगवान शिव");
        addVocab("muruga", "முருகப்பெருமான்", "మురుగన్ స్వామి", "भगवान मुरुगन");
        addVocab("murugan", "முருகப்பெருமான்", "మురుగన్ స్వామి", "भगवान मुरुगन");
        addVocab("arumuga nayinar", "ஆறுமுக நயினார்", "ఆరుముఖ నాయినార్", "आरुमुग नायिनार");
        addVocab("arumuga", "ஆறுமுக", "ఆరుముఖ", "आरुमुग");
        addVocab("nayinar", "நயினார்", "నాయినార్", "नायिनार");
        addVocab("anjali varatha anjaneyar", "அஞ்சலி வரத ஆஞ்சநேயர்", "అంజలి వరద ఆంజనేయ స్వామి", "अंजलि वरद आंजनेय");
        addVocab("anjaneyar", "ஆஞ்சநேயர்", "ఆంజనేయ స్వామి", "हनुमान जी");
        addVocab("hanuman", "ஆஞ்சநேயர்", "హనుమంతుడు", "हनुमान जी");
        addVocab("anjali", "அஞ்சலி", "అంజలి", "अंजलि");
        addVocab("varatha", "வரத", "వరద", "वरद");
        addVocab("varadar", "வரதர்", "వరదరాజ", "वरदराज");
        addVocab("perumal", "பெருமாள்", "పెరుమాళ్", "भगवान विष्णु");
        addVocab("vishnu", "விஷ்ணு", "విష్ణువు", "भगवान विष्णु");
        addVocab("ganesha", "விநாயகர்", "వినాయకుడు", "गणेश जी");
        addVocab("vinayagar", "விநாயகர்", "వినాయకుడు", "गणेश जी");
        addVocab("amman", "அம்மன்", "అమ్మవారు", "अम्मन");
        addVocab("thayar", "தாயார்", "తాయారు", "थायार");
        addVocab("swamy", "சுவாமி", "స్వామి", "स्वामी");
        addVocab("swami", "சுவாமி", "స్వామి", "स्वामी");
        addVocab("oppiliappan", "ஒப்பிலியப்பன்", "ఒప్పిలియప్పన్", "ओप्पिलियप्पन");
        addVocab("parthasarathy", "பார்த்தசாரதி", "పార్థసారథి", "पार्थसारथी");
        addVocab("mallikarjunar", "மல்லிகார்ஜுனர்", "మల్లికార్జునుడు", "मल्लिकार्जुन");
        addVocab("idumban", "இடும்பன்", "ఇడుంబన్", "इडुंबन");
        addVocab("devanatha", "தேவநாத", "దేవనాథ", "देवनाथ");
        addVocab("ashtabhuja", "அஷ்டபுஜ", "అష్టభుజ", "अष्टभुज");
        addVocab("vadaranyeswarar", "வாடாரண்யேஸ்வரர்", "వాడారణ్యేశ్వరుడు", "वादारण्येश्वर");
        addVocab("kannanore", "கண்ணனூர்", "కన్ననూర్", "कन्ननोर");
        addVocab("mari amman", "மாரியம்மன்", "మారి అమ్మవారు", "मारी अम्मन");
        addVocab("prasana", "பிரசன்ன", "ప్రసన్న", "प्रसन्न");
        addVocab("prasanna", "பிரசன்ன", "ప్రసన్న", "प्रसन्न");
        addVocab("venketasar", "வெங்கடேசர்", "వెంకటేశ్వరుడు", "वेंकटेश");
        addVocab("venkatesa", "வெங்கடேசர்", "వెంకటేశ్వరుడు", "वेंकटेश");
        addVocab("vellimalai nathar", "வெள்ளிமலை நாதர்", "వెండికొండ నాథుడు", "वेल्लिमलाई नाथ");
        addVocab("nellivananathar", "நெல்லிவனநாதர்", "నెల్లివననాథుడు", "नेल्लीवननाथर");
        addVocab("varthamaneeswarar", "வர்த்தமானீஸ்வரர்", "వర్ధమానేశ్వరుడు", "वर्धमानेश्वर");
        addVocab("ekambareswarar", "ஏகாம்பரேஸ்வரர்", "ఏకాంబరేశ్వరుడు", "एकाम्रेश्वर");
        addVocab("swaminatha", "சுவாமிநாத", "స్వామినాథ", "स्वामीनाथ");
        addVocab("pureeswarar", "புரீஸ்வரர்", "పురీశ్వరుడు", "पुरीश्वर");
        addVocab("kayaroganeswarar", "காயாரோகணேஸ்வரர்", "కాయారోహణేశ్వరుడు", "कायारोहणेश्वर");
        addVocab("thanthondreeswarar", "தான்தோன்றீஸ்வரர்", "తాంతోండ్రీశ్వరుడు", "तानथोंड्रीश्वर");
        addVocab("neelakandeswarar", "நீலகண்டேஸ்வரர்", "నీలకంఠేశ్వరుడు", "नीलकंठेश्वर");
        addVocab("pallavaneswarar", "பல்லவனேஸ்வரர்", "పల్లవనేశ్వరుడు", "पल्लवनेश्वर");
        addVocab("aranyeswarar", "அரண்யேஸ்வரர்", "అరణ్యేశ్వరుడు", "अरण्येश्वर");
        addVocab("naganathar", "நாகநாதர்", "నాగనాథుడు", "नागनाथ");
        addVocab("uthavedeeswarar", "உத்தவேதீஸ்வரர்", "ఉత్తవేదీశ్వరుడు", "उत्तवेदीश्वर");
        addVocab("mayuranathar", "மயூூரநாதர்", "మయూరనాథుడు", "मयूरनाथ");
        addVocab("valampura nathar", "வலம்புர நாதர்", "వలంబుర నాథుడు", "वलंबुर नाथ");
        addVocab("eswarar", "ஈஸ்வரர்", "ఈశ్వరుడు", "ईश्वर");
        addVocab("eeswarar", "ஈஸ்வரர்", "ఈశ్వరుడు", "ईश्वर");
        addVocab("iswarar", "ஈஸ்வரர்", "ఈశ్వరుడు", "ईश्वर");
        addVocab("nathar", "நாதர்", "నాథుడు", "नाथ");

        // Cities, Districts & Locations
        addVocab("chinnalapatti", "சின்னாளப்பட்டி", "చిన్నాలపట్టి", "चिन्नालपट्टी");
        addVocab("dindigul", "திண்டுக்கல்", "దిండిగల్", "डिंडीगुल");
        addVocab("tamil nadu", "தமிழ்நாடு", "తమిళనాడు", "तमिलनाडु");
        addVocab("madurai", "மதுரை", "మధురై", "मदुरै");
        addVocab("thanjavur", "தஞ்சாவூர்", "తంజావూరు", "तंजावुर");
        addVocab("chennai", "சென்னை", "చెన్నై", "चेन्नई");
        addVocab("triplicane", "திருவல்லிக்கேணி", "ట్రిప్లికేన్", "ट्रिप्लिकेन");
        addVocab("trichy", "திருச்சிராப்பள்ளி", "తిరుచిరాపల్లి", "तिरुचिरापल्ली");
        addVocab("tiruchirappalli", "திருச்சிராப்பள்ளி", "తిరుచిరాపల్లి", "तिरुचिरापल्ली");
        addVocab("coimbatore", "கோயம்புத்தூர்", "కోయంబత్తూరు", "कोयंबटूर");
        addVocab("salem", "சேலம்", "సేలం", "सेलम");
        addVocab("tirunelveli", "திருநெல்வேலி", "తిరునెల్వేలి", "तिरुनेलवेली");
        addVocab("kanchipuram", "காஞ்சிபுரம்", "కాంచీపురం", "कांचीपुरम");
        addVocab("rameswaram", "ராமேஸ்வரம்", "రామేశ్వరం", "रामेश्वरम");
        addVocab("palani", "பழனி", "పళని", "पलानी");
        addVocab("tiruchendur", "திருச்செந்தூர்", "తిరుచెందూర్", "तिरुचेंदूर");
        addVocab("chidambaram", "சிதம்பரம்", "చిదంబరం", "चिदंबरम");
        addVocab("kumbakonam", "கும்பகோணம்", "కుంభకోణం", "कुंभकोणम");
        addVocab("theni", "தேனி", "తేని", "थेनी");
        addVocab("kodangipatti", "கோடங்கிபட்டி", "కోడంగిపట్టి", "कोडंगिपट्टी");
        addVocab("mayiladuthurai", "மயிலாடுதுறை", "మయిలాడుదురై", "मयिलादुथुरै");
        addVocab("nagapattinam", "நாகப்பட்டினம்", "నాగపట్నం", "नागपट्टिनम");
        addVocab("tiruvarur", "திருவாரூர்", "తిరువారూరు", "तिरुवारूर");
        addVocab("cuddalore", "கடலூர்", "కడలూరు", "कडलूर");
        addVocab("kurnool", "கர்நூல்", "కర్నూలు", "कुरनूल");
        addVocab("srishailam", "ஸ்ரீசைலம்", "శ్రీశైలం", "श्रीशैलम");
        addVocab("srisailam", "ஸ்ரீசைலம்", "శ్రీశైలం", "श्रीशैलम");
        addVocab("tiruvattathurai", "திருவட்டத்துறை", "తిరువట్టత్తురై", "तिरुवट्टथुराई");
        addVocab("aakkoor", "ஆக்கூர்", "ఆక్కూర్", "आक्कूर");
        addVocab("iluppaipattu", "இலுப்பைப்பட்டு", "ఇలుప్పైపట్టు", "इलुप्पैपट्टु");
        addVocab("poompuhar", "பூம்புகார்", "పూంపుహార్", "पूमपुहार");
        addVocab("tirukattupalli", "திருக்காட்டுப்பள்ளி", "తిరుక్కాట్టుపల్లి", "तिरुक्काट्टुपल्ली");
        addVocab("kilperumpallam", "கீழப்பெரும்பள்ளம்", "కీళపెరుంపల్లం", "कीळपेरुमपल्लम");
        addVocab("kuthalam", "குத்தாலம்", "కుత్తాలం", "कुत्तालम");
        addVocab("melaperumpallam", "மேலப்பெரும்பள்ளம்", "మేలపెరుంపల్లం", "मेलपेरुमपल्लम");
        addVocab("tirunageswaram", "திருநாகேஸ்வரம்", "తిరునాగేశ్వరం", "तिरुनागेश्वरम");
        addVocab("kovai kumaran kottam", "கோவை குமரன்கோட்டம்", "కోయంబత్తూరు కుమరన్ కోట్టం", "कोयंबटूर कुमारन कोट्टम");
        addVocab("maanandakudi", "மானந்தக்குடி", "మానందకుడి", "मानंदकुडी");
        addVocab("thirupugalur", "திருப்புகலூர்", "తిరుపుగలూరు", "तिरुपुगलूर");
        addVocab("thiru thangur", "திருத்தங்கூர்", "తిరుత్తంగూరు", "तिरुत्तंगूर");
        addVocab("tirunellikka", "திருநெல்லிக்கா", "తిరునెల్లిక్కా", "तिरुनेल्लिक्का");
        addVocab("tirumalai vaiyavur", "திருமலை வையாவூர்", "తిరుమలై వైయావూరు", "तिरुमलै वैयावूर");
        addVocab("thiruvaheendrapuram", "திருவஹீந்திரபுரம்", "తిరువహీంద్రపురం", "तिरुवहींद्रपुरम");

        // Common temple phrases
        addVocab("500 years old", "500 ஆண்டுகள் பழமையானது", "500 సంవత్సరాల పురాతనమైనది", "500 वर्ष पुराना");
        addVocab("1000 years old", "1000 ஆண்டுகள் பழமையானது", "1000 సంవత్సరాల పురాతనమైనది", "1000 वर्ष पुराना");
        addVocab("500-1000 years old", "500-1000 ஆண்டுகள் பழமையானது", "500-1000 సంవత్సరాల పురాతనమైనది", "500-1000 वर्ष पुराना");
        addVocab("1000-2000 years old", "1000-2000 ஆண்டுகள் பழமையானது", "1000-2000 సంవత్సరాల పురాతనమైనది", "1000-2000 वर्ष पुराना");
        addVocab("2000 years old", "2000 ஆண்டுகள் பழமையானது", "2000 సంవత్సరాల పురాతనమైనది", "2000 वर्ष पुराना");
        addVocab("years old", "ஆண்டுகள் பழமையானது", "సంవత్సరాల పురాతనమైనది", "वर्ष पुराना");
        addVocab("year old", "ஆண்டுகள் பழமையானது", "సంవత్సరాల పురాతనమైనది", "वर्ष पुराना");
        addVocab("century", "நூற்றாண்டு", "శతాబ్దం", "शताब्दी");
        addVocab("ancient", "தொன்மை வாய்ந்தது", "పురాతనమైనది", "प्राचीन");
        addVocab("railway station", "இரயில் நிலையம்", "రైల్వే స్టేషన్", "रेलवे स्टेशन");
        addVocab("airport", "விமான நிலையம்", "విమానాశ్రయం", "हवाई अड्डा");
        addVocab("available", "கிடைக்கும்", "లభిస్తుంది", "उपलब्ध");
        addVocab("nearby", "அருகில்", "సమీపంలో", "के निकट");
        addVocab("near by", "அருகில்", "సమీపంలో", "के निकट");
        addVocab("district", "மாவட்டம்", "జిల్లా", "जिला");
        addVocab("state", "மாநிலம்", "రాష్ట్రం", "राज्य");
        addVocab("devotees pray", "பக்தர்கள் பிரார்த்தனை செய்கிறார்கள்", "భక్తులు ప్రార్థిస్తారు", "भक्त प्रार्थना करते हैं");
        addVocab("abishek", "அபிஷேகம்", "அభిషేకం", "अभिषेक");
        addVocab("pooja", "பூஜை", "పూజ", "पूजा");
        addVocab("festival", "திருவிழா", "ఉత్సవం", "त्योहार");
        addVocab("prayers", "பிரார்த்தனைகள்", "ప్రార్థనలు", "प्रार्थनाएँ");
        addVocab("thanks giving", "நேர்த்திக்கடன்", "మొక్కుబడులు", "कृतज्ञता अर्पण");
        addVocab("features", "சிறப்பம்சங்கள்", "విశేషాలు", "विशेषताएं");
        addVocab("greatness", "பெருமை", "గొప్పతనం", "महिमा");
        addVocab("history", "வரலாறு", "చరిత్ర", "इतिहास");
        addVocab("speciality", "சிறப்பு", "ప్రత్యేకత", "विशेषता");

        // Sort descending by length so longer phrases match first
        VOCAB_TA.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        VOCAB_TE.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        VOCAB_HI.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
    }

    public Temple translate(Temple original, String targetLang) {
        if (original == null) return null;
        String lang = (targetLang != null && !targetLang.isBlank()) ? targetLang.toLowerCase().trim() : "ta";
        if ("en".equals(lang)) return original;

        return new Temple(
            original.id(),
            translateField(original.name(), lang, true),
            translateField(original.moolavar(), lang, false),
            translateField(original.urchavar(), lang, false),
            translateField(original.ammanThayar(), lang, false),
            translateField(original.thalaVirutcham(), lang, false),
            translateField(original.theertham(), lang, false),
            translateField(original.agamamPooja(), lang, false),
            translateField(original.oldYear(), lang, false),
            translateField(original.historicalName(), lang, false),
            translateField(original.city(), lang, false),
            translateField(original.district(), lang, false),
            translateField(original.state(), lang, false),
            translateField(original.singers(), lang, false),
            translateField(original.festival(), lang, false),
            translateField(original.generalInformation(), lang, false),
            translateField(original.address(), lang, false),
            original.phone(),
            translateField(original.openingTime(), lang, false),
            translateField(original.speciality(), lang, false),
            translateField(original.prayers(), lang, false),
            translateField(original.thanksGiving(), lang, false),
            translateField(original.greatness(), lang, false),
            translateField(original.history(), lang, false),
            translateField(original.features(), lang, false),
            original.hfLat(),
            original.hfLan(),
            translateField(original.location(), lang, false),
            translateField(original.nearByAirport(), lang, false),
            translateField(original.nearByRailwayStation(), lang, false),
            translateField(original.accommodation(), lang, false)
        );
    }

    public String translateField(String text, String lang, boolean isName) {
        if (text == null || text.isBlank()) return text;
        String trimmed = text.trim();
        if (trimmed.equals("-") || trimmed.equals(".")) return text;

        List<Map.Entry<String, String>> vocab = switch (lang) {
            case "te" -> VOCAB_TE;
            case "hi" -> VOCAB_HI;
            default -> VOCAB_TA;
        };

        String result = trimmed;
        for (Map.Entry<String, String> entry : vocab) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(entry.getKey()) + "\\b");
            result = p.matcher(result).replaceAll(entry.getValue());
        }

        // If any remaining Latin characters exist, transliterate phonetically
        if (result.matches(".*[a-zA-Z].*")) {
            result = transliterateRemainingLatin(result, lang);
        }

        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    private String transliterateRemainingLatin(String text, String lang) {
        String[] tokens = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.matches(".*[a-zA-Z].*")) {
                sb.append(transliterateWord(token, lang));
            } else {
                sb.append(token);
            }
            if (i < tokens.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private static final String[][] CONSONANTS_TA = {
        {"sh", "ஷ"}, {"th", "த"}, {"dh", "த"}, {"ch", "ச"}, {"zh", "ழ"},
        {"bh", "ப"}, {"ph", "ப"}, {"kh", "க"}, {"gh", "க"},
        {"k", "க"}, {"g", "க"}, {"s", "ச"}, {"j", "ஜ"},
        {"t", "ட"}, {"d", "ட"}, {"n", "ந"}, {"p", "ப"},
        {"b", "ப"}, {"m", "ம"}, {"y", "ய"}, {"r", "ர"},
        {"l", "ல"}, {"v", "வ"}, {"w", "வ"}, {"h", "ஹ"}
    };

    private static final String[][] VOWELS_TA = {
        {"aa", "ா"}, {"ee", "ீ"}, {"ii", "ீ"}, {"oo", "ோ"}, {"uu", "ூ"},
        {"ai", "ை"}, {"au", "ௌ"},
        {"a", ""}, {"e", "ெ"}, {"i", "ி"}, {"o", "ொ"}, {"u", "ு"}
    };

    private static final String[][] INITIAL_VOWELS_TA = {
        {"aa", "ஆ"}, {"ee", "ஈ"}, {"ii", "ஈ"}, {"oo", "ஓ"}, {"uu", "ஊ"},
        {"ai", "ஐ"}, {"au", "ஔ"},
        {"a", "அ"}, {"e", "எ"}, {"i", "இ"}, {"o", "ஒ"}, {"u", "உ"}
    };

    private String transliterateWord(String word, String lang) {
        String clean = word.toLowerCase().trim();
        StringBuilder res = new StringBuilder();
        int idx = 0;

        for (String[] iv : INITIAL_VOWELS_TA) {
            if (clean.startsWith(iv[0])) {
                res.append(iv[1]);
                idx += iv[0].length();
                break;
            }
        }

        while (idx < clean.length()) {
            boolean matchedC = false;
            for (String[] c : CONSONANTS_TA) {
                if (clean.substring(idx).startsWith(c[0])) {
                    res.append(c[1]);
                    idx += c[0].length();
                    matchedC = true;

                    boolean matchedV = false;
                    for (String[] v : VOWELS_TA) {
                        if (clean.substring(idx).startsWith(v[0])) {
                            res.append(v[1]);
                            idx += v[0].length();
                            matchedV = true;
                            break;
                        }
                    }
                    if (!matchedV && idx < clean.length()) {
                        res.append("்");
                    }
                    break;
                }
            }
            if (!matchedC) {
                boolean matchedV = false;
                for (String[] v : VOWELS_TA) {
                    if (clean.substring(idx).startsWith(v[0])) {
                        res.append(v[1]);
                        idx += v[0].length();
                        matchedV = true;
                        break;
                    }
                }
                if (!matchedV) {
                    idx++;
                }
            }
        }
        return res.length() > 0 ? res.toString() : word;
    }
}

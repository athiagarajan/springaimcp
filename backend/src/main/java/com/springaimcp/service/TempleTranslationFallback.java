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
        // Titles & Temple generic terms
        addVocab("sri", "ஸ்ரீ", "శ్రీ", "श्री");
        addVocab("shri", "ஸ்ரீ", "శ్రీ", "श्री");
        addVocab("temple", "திருக்கோயில்", "ఆలయం", "मंदिर");
        addVocab("kovil", "கோயில்", "కోవిల్", "मंदिर");
        addVocab("shrine", "திருக்கோயில்", "ఆలయం", "मंदिर");
        addVocab("lord", "இறைவன்", "భగవంతుడు", "भगवान");

        // Deities
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
        addVocab("ganapathy", "விநாயகர்", "గణపతి", "गणपति");
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
        addVocab("venkateswarar", "வெங்கடேஸ்வரர்", "వెంకటేశ్వరుడు", "वेंकटेश्वर");
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

        // Saints, Poets, Kings & Devotees (People Names)
        addVocab("arunagirinathar", "அருணகிரிநாதர்", "అరుణగిరినాథుడు", "अरुणागिरिनाथ");
        addVocab("thirugnanasambandar", "திருஞானசம்பந்தர்", "తిరుజ్ఞానసంబంధర్", "तिरुज्ञानसंबंधर");
        addVocab("sambandar", "சம்பந்தர்", "సంబంధర్", "संबंधर");
        addVocab("thirunavukkarasar", "திருநாவுக்கரசர்", "తిరునావుక్కరసర్", "तिरुनावुक्करासर");
        addVocab("appar", "அப்பர்", "అప్పర్", "अप्पर");
        addVocab("sundarar", "சுந்தரர்", "సుందరర్", "सुंदरर");
        addVocab("manickavasagar", "மாணிக்கவாசகர்", "మాణిక్యవాచకర్", "माणिक्यवाचकर");
        addVocab("manikkavasagar", "மாணிக்கவாசகர்", "మాణిక్యవాచకర్", "माणिक्यवाचकर");
        addVocab("ramanujar", "ராமானுஜர்", "రామానుజుడు", "रामानुज");
        addVocab("ramanuja", "ராமானுஜர்", "రామానుజుడు", "रामानुज");
        addVocab("agasthiyar", "அகத்தியர்", "అగస్త్యుడు", "अगस्त्य");
        addVocab("agastya", "அகத்தியர்", "అగస్త్యుడు", "अगस्त्य");
        addVocab("adi shankara", "ஆதி சங்கரர்", "ఆది శంకరుడు", "आदि शंकर");
        addVocab("shankara", "சங்கரர்", "శంకరుడు", "शंकर");
        addVocab("thirumangai azhwar", "திருமங்கையாழ்வார்", "తిరుమంగై ఆళ్వార్", "तिरुमंगई आलवार");
        addVocab("thirumangai", "திருமங்கை", "తిరుమంగై", "तिरुमंगई");
        addVocab("nammazhwar", "நம்மாழ்வார்", "నమ్మాళ్వార్", "नम्मालवार");
        addVocab("namalwar", "நம்மாழ்வார்", "నమ్మాళ్వార్", "नम्मालवार");
        addVocab("azhwar", "ஆழ்வார்", "ఆళ్వార్", "आलवार");
        addVocab("alwar", "ஆழ்வார்", "ఆళ్వార్", "आलवार");
        addVocab("peyazhwar", "பேயாழ்வார்", "పేయాళ్వార్", "पेयालवार");
        addVocab("bhoothathazhwar", "பூதத்தாழ்வார்", "భూతత్తాళ్వార్", "भूतत्तालवार");
        addVocab("poigai azhwar", "பொய்கையாழ்வார்", "పొయ్గై ఆళ్వార్", "पोयगई आलवार");
        addVocab("andal", "ஆண்டாள்", "ఆండాళ్", "आंडाल");
        addVocab("periyazhwar", "பெரியாழ்வார்", "పెరియాళ్వార్", "पेरियालवार");
        addVocab("kulasekara azhwar", "குலசேகர ஆழ்வார்", "కులశేఖర ఆళ్వార్", "कुलशेखर आलवार");
        addVocab("thirumalisai azhwar", "திருமழிசை ஆழ்வார்", "తిరుమళిశై ఆళ్వార్", "तिरुमळिशै आलवार");
        addVocab("thondaradippodi", "தொண்டரடிப்பொடி ஆழ்வார்", "తొండరడిప్పొడి ఆళ్వార్", "तौंडराडिपौडि आलवार");
        addVocab("thiruppan azhwar", "திருப்பாணாழ்வார்", "తిరుప్పాణాళ్వార్", "तिरुप्पाणालवार");
        addVocab("madhurakavi azhwar", "மதுரகவி ஆழ்வார்", "మధురకవి ఆళ్వార్", "मधुरकवि आलवार");
        addVocab("nayanmar", "நாயன்மார்", "నాయనార్లు", "नायनार");
        addVocab("nayanmars", "நாயன்மார்கள்", "నాయనార్లు", "नायनार");
        addVocab("chola", "சோழர்", "చోళులు", "चोल");
        addVocab("cholas", "சோழர்கள்", "చోళులు", "चोल");
        addVocab("pandya", "பாண்டியர்", "పాండ్యులు", "पांड्य");
        addVocab("pandyas", "பாண்டியர்கள்", "పాండ్యులు", "पांड्य");
        addVocab("pallava", "பல்லவர்", "పల్లవులు", "पल्लव");
        addVocab("pallavas", "பல்லவர்கள்", "పల్లవులు", "पल्लव");
        addVocab("cheran", "சேரர்", "చేరులు", "चेर");
        addVocab("chera", "சேரர்", "చేరులు", "चेर");
        addVocab("nayak", "நாயக்கர்", "నాయకులు", "नायक");
        addVocab("nayakar", "நாயக்கர்", "నాయకులు", "नायक");
        addVocab("raja raja chola", "ராஜராஜ சோழன்", "రాజరాజ చోళుడు", "राजराज चोल");
        addVocab("kulothunga", "குலோத்துங்கன்", "కులోత్తుంగుడు", "कुलोत्तुंग");
        addVocab("vijayanagara", "விஜயநகர", "విజయనగర", "विजयनगर");

        // Cities, Districts & Locations (Place Names)
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
        addVocab("theertha thotti", "தீர்த்தத் தொட்டி", "తీర్థ తొట్టి", "तीर्थ तोट्टी");
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
        addVocab("tiruvannamalai", "திருவண்ணாமலை", "తిరువణ్ణామలై", "तिरुवन्नामलाई");
        addVocab("thiruvannamalai", "திருவண்ணாமலை", "తిరువణ్ణామలై", "तिरुवन्नामलाई");
        addVocab("kallidaikurichi", "கல்லிடைக்குறிச்சி", "కల్లిడైకురిచి", "कल्लिदैकुरिची");
        addVocab("vedaranyam", "வேதாரண்யம்", "వేదారణ్యం", "वेदारण्यम");
        addVocab("srirangam", "ஸ்ரீரங்கம்", "శ్రీరంగం", "श्रीरंगम");
        addVocab("samayapuram", "சமயபுரம்", "సమయపురం", "समयपुरम");
        addVocab("swamimalai", "சுவாமிமலை", "స్వామిమలై", "स्वामीमलाई");
        addVocab("tiruttani", "திருத்தணி", "తిరుత్తణి", "तिरुत्तणी");
        addVocab("thiruttani", "திருத்தணி", "తిరుత్తణి", "तिरुत्तणी");
        addVocab("thiruparankundram", "திருப்பரங்குன்றம்", "తిరుప్పరన్కుండ్రం", "तिरुप्परनकुंद्रम");
        addVocab("pazhamudircholai", "பழமுதிர்சோலை", "పళముదిర్చోలై", "पळमुदिरचोलै");
        addVocab("tirunallar", "திருநள்ளாறு", "తిరునల్లారు", "तिरुनाल्लारु");
        addVocab("thirunallar", "திருநள்ளாறு", "తిరునల్లారు", "तिरुनाल्लारु");
        addVocab("tirupati", "திருப்பதி", "తిరుపతి", "तिरुपति");
        addVocab("srivilliputhur", "ஸ்ரீவில்லிபுத்தூர்", "శ్రీవిల్లిపుత్తూరు", "श्रीविल्लिपुत्तूर");
        addVocab("kanyakumari", "கன்னியாகுமரி", "కన్యాకుమారి", "कन्याकुमारी");
        addVocab("suchindram", "சுசீந்திரம்", "సుచీంద్రం", "सुचिंद्रम");
        addVocab("tenkasi", "தென்காசி", "తెన్కాశి", "तेनकाशी");
        addVocab("courtallam", "குற்றாலம்", "కుర్తాళం", "कुट्रालम");
        addVocab("kutralam", "குற்றாலம்", "కుర్తాళం", "कुट्रालम");
        addVocab("villupuram", "விழுப்புரம்", "విల్లుపురం", "विल्लुपुरम");
        addVocab("vellore", "வேலூர்", "వేలూరు", "वेल्लोर");
        addVocab("tiruvallur", "திருவள்ளூர்", "తిరువళ్లూరు", "तिरुवल्लूर");
        addVocab("thiruvallur", "திருவள்ளூர்", "తిరువళ్లూరు", "तिरुवल्लूर");
        addVocab("dharmapuri", "தர்மபுரி", "ధర్మపురి", "धर्मपुरी");
        addVocab("krishnagiri", "கிருஷ்ணகிரி", "కృష్ణగిరి", "कृष्णगिरि");
        addVocab("namakkal", "நாமக்கல்", "నామక్కల్", "नामक्कल");
        addVocab("karur", "கரூர்", "కరూర్", "करूर");
        addVocab("perambalur", "பெரம்பலூர்", "పెరంబలూరు", "पेरांबलूर");
        addVocab("ariyalur", "அரியலூர்", "అరియలూరు", "अरियालूर");
        addVocab("pudukkottai", "புதுக்கோட்டை", "పుదుక్కోట్టై", "पुदुक्कोट्टई");
        addVocab("sivaganga", "சிவகங்கை", "శివగంగ", "शिवगंगा");
        addVocab("ramanathapuram", "ராமநாதபுரம்", "రామనాథపురం", "रामनाथपुरम");
        addVocab("virudhunagar", "விருதுநகர்", "విరుదునగర్", "विरुद्धनगर");
        addVocab("thoothukudi", "தூத்துக்குடி", "తూత్తుకుడి", "थूथुकुडी");
        addVocab("tuticorin", "தூத்துக்குடி", "తూత్తుకుడి", "थूथुकुडी");
        addVocab("nilgiris", "நீலகிரி", "నీలగిరి", "नीलगिरि");

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
        addVocab("abishek", "அபிஷேகம்", "అభిషేకం", "अभिषेक");
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

        // Apply smart place and name affixes for any remaining compound words
        result = applySmartAffixes(result, lang);

        // If any remaining Latin characters exist, transliterate phonetically using the target script
        if (result.matches(".*[a-zA-Z].*")) {
            result = transliterateRemainingLatin(result, lang);
        }

        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    private static final String[][] AFFIXES_TA = {
        {"thiruvanna", "திருவண்ணா"}, {"tiruvanna", "திருவண்ணா"},
        {"thiru", "திரு"}, {"tiru", "திரு"},
        {"malai", "மலை"}, {"patti", "பட்டி"}, {"thotti", "தொட்டி"},
        {"puram", "புரம்"}, {"kulam", "குளம்"}, {"kudi", "குடி"},
        {"oor", "ஊர்"}, {"ur", "ஊர்"}
    };

    private static final String[][] AFFIXES_TE = {
        {"thiruvanna", "తిరువణ్ణా"}, {"tiruvanna", "తిరువణ్ణా"},
        {"thiru", "తిరు"}, {"tiru", "తిరు"},
        {"malai", "మలై"}, {"patti", "పట్టి"}, {"thotti", "తొట్టి"},
        {"puram", "పురం"}, {"kulam", "కుళం"}, {"kudi", "కుడి"},
        {"oor", "ఊరు"}, {"ur", "ఊరు"}
    };

    private static final String[][] AFFIXES_HI = {
        {"thiruvanna", "तिरुवन्ना"}, {"tiruvanna", "तिरुवन्ना"},
        {"thiru", "तिरु"}, {"tiru", "तिरु"},
        {"malai", "मलाई"}, {"patti", "पट्टी"}, {"thotti", "तोट्टी"},
        {"puram", "पुरम"}, {"kulam", "कुलम"}, {"kudi", "कुडी"},
        {"oor", "ऊर"}, {"ur", "ऊर"}
    };

    private String applySmartAffixes(String text, String lang) {
        String[][] affixes = switch (lang) {
            case "te" -> AFFIXES_TE;
            case "hi" -> AFFIXES_HI;
            default -> AFFIXES_TA;
        };
        String res = text;
        for (String[] pair : affixes) {
            Pattern p = Pattern.compile("(?i)" + Pattern.quote(pair[0]));
            res = p.matcher(res).replaceAll(pair[1]);
        }
        return res;
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

    // 1. TAMIL SCRIPT MAPPINGS
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

    // 2. TELUGU SCRIPT MAPPINGS
    private static final String[][] CONSONANTS_TE = {
        {"sh", "శ"}, {"th", "త"}, {"dh", "ద"}, {"ch", "చ"}, {"zh", "ళ"},
        {"bh", "భ"}, {"ph", "ఫ"}, {"kh", "ఖ"}, {"gh", "ఘ"},
        {"k", "క"}, {"g", "గ"}, {"s", "స"}, {"j", "జ"},
        {"t", "ట"}, {"d", "డ"}, {"n", "న"}, {"p", "ప"},
        {"b", "బ"}, {"m", "మ"}, {"y", "య"}, {"r", "ర"},
        {"l", "ల"}, {"v", "వ"}, {"w", "వ"}, {"h", "హ"}
    };
    private static final String[][] VOWELS_TE = {
        {"aa", "ా"}, {"ee", "ీ"}, {"ii", "ీ"}, {"oo", "ో"}, {"uu", "ూ"},
        {"ai", "ై"}, {"au", "ౌ"},
        {"a", ""}, {"e", "ె"}, {"i", "ి"}, {"o", "ొ"}, {"u", "ు"}
    };
    private static final String[][] INITIAL_VOWELS_TE = {
        {"aa", "ఆ"}, {"ee", "ఈ"}, {"ii", "ఈ"}, {"oo", "ఓ"}, {"uu", "ఊ"},
        {"ai", "ఐ"}, {"au", "ఔ"},
        {"a", "అ"}, {"e", "ఎ"}, {"i", "ఇ"}, {"o", "ఒ"}, {"u", "ఉ"}
    };

    // 3. HINDI (DEVANAGARI) SCRIPT MAPPINGS
    private static final String[][] CONSONANTS_HI = {
        {"sh", "श"}, {"th", "थ"}, {"dh", "ध"}, {"ch", "च"}, {"zh", "झ"},
        {"bh", "भ"}, {"ph", "फ"}, {"kh", "ख"}, {"gh", "घ"},
        {"k", "क"}, {"g", "ग"}, {"s", "स"}, {"j", "ज"},
        {"t", "ट"}, {"d", "ड"}, {"n", "न"}, {"p", "प"},
        {"b", "ब"}, {"m", "म"}, {"y", "य"}, {"r", "र"},
        {"l", "ल"}, {"v", "व"}, {"w", "व"}, {"h", "ह"}
    };
    private static final String[][] VOWELS_HI = {
        {"aa", "ा"}, {"ee", "ी"}, {"ii", "ी"}, {"oo", "ो"}, {"uu", "ू"},
        {"ai", "ै"}, {"au", "ौ"},
        {"a", ""}, {"e", "े"}, {"i", "ि"}, {"o", "ो"}, {"u", "ु"}
    };
    private static final String[][] INITIAL_VOWELS_HI = {
        {"aa", "आ"}, {"ee", "ई"}, {"ii", "ई"}, {"oo", "ओ"}, {"uu", "ऊ"},
        {"ai", "ऐ"}, {"au", "औ"},
        {"a", "अ"}, {"e", "ए"}, {"i", "इ"}, {"o", "ओ"}, {"u", "उ"}
    };

    private String transliterateWord(String word, String lang) {
        String clean = word.toLowerCase().trim();
        if (clean.isBlank()) return "";

        String[][] consonants;
        String[][] vowels;
        String[][] initials;
        String halant;

        switch (lang) {
            case "te" -> {
                consonants = CONSONANTS_TE;
                vowels = VOWELS_TE;
                initials = INITIAL_VOWELS_TE;
                halant = "్";
            }
            case "hi" -> {
                consonants = CONSONANTS_HI;
                vowels = VOWELS_HI;
                initials = INITIAL_VOWELS_HI;
                halant = "्";
            }
            default -> {
                consonants = CONSONANTS_TA;
                vowels = VOWELS_TA;
                initials = INITIAL_VOWELS_TA;
                halant = "்";
            }
        }

        StringBuilder res = new StringBuilder();
        int idx = 0;

        for (String[] iv : initials) {
            if (clean.startsWith(iv[0])) {
                res.append(iv[1]);
                idx += iv[0].length();
                break;
            }
        }

        while (idx < clean.length()) {
            boolean matchedC = false;
            for (String[] c : consonants) {
                if (clean.substring(idx).startsWith(c[0])) {
                    res.append(c[1]);
                    idx += c[0].length();
                    matchedC = true;

                    boolean matchedV = false;
                    for (String[] v : vowels) {
                        if (clean.substring(idx).startsWith(v[0])) {
                            res.append(v[1]);
                            idx += v[0].length();
                            matchedV = true;
                            break;
                        }
                    }
                    if (!matchedV && idx < clean.length()) {
                        res.append(halant);
                    }
                    break;
                }
            }
            if (!matchedC) {
                boolean matchedV = false;
                for (String[] v : vowels) {
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

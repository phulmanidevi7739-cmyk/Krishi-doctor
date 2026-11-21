package com.example.data

import androidx.annotation.DrawableRes
import com.example.R
import com.example.model.AppLanguage

enum class DiseasePathogenType {
    FUNGAL,
    BACTERIAL,
    VIRAL,
    PEST_COMPLEX
}

data class CropDiseaseEntry(
    val titleHi: String,
    val titleEn: String,
    val pathogenType: DiseasePathogenType,
    val symptomHi: String,
    val symptomEn: String,
    val detailedSymptomsHi: List<String>,
    val detailedSymptomsEn: List<String>,
    val favorableWeatherHi: String,
    val favorableWeatherEn: String,
    val tipHi: String,
    val tipEn: String
) {
    fun title(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) titleHi else titleEn

    fun pathogenLabel(language: AppLanguage): String = when (pathogenType) {
        DiseasePathogenType.FUNGAL -> if (language == AppLanguage.HINDI) "फफूँद" else "Fungal"
        DiseasePathogenType.BACTERIAL -> if (language == AppLanguage.HINDI) "जीवाणु" else "Bacterial"
        DiseasePathogenType.VIRAL -> if (language == AppLanguage.HINDI) "विषाणु" else "Viral"
        DiseasePathogenType.PEST_COMPLEX -> if (language == AppLanguage.HINDI) "कीट / वायरस" else "Pest / Viral"
    }

    fun symptom(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) symptomHi else symptomEn

    fun detailedSymptoms(language: AppLanguage): List<String> =
        if (language == AppLanguage.HINDI) detailedSymptomsHi else detailedSymptomsEn

    fun favorableWeather(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) favorableWeatherHi else favorableWeatherEn

    fun tip(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) tipHi else tipEn
}

data class CropDiseaseGuide(
    val cropId: String,
    @DrawableRes val imageResId: Int,
    val nameHi: String,
    val nameEn: String,
    val categoryHi: String,
    val categoryEn: String,
    val identificationHintHi: String,
    val identificationHintEn: String,
    val basicCareGuideHi: String,
    val basicCareGuideEn: String,
    val diseases: List<CropDiseaseEntry>
) {
    fun name(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) nameHi else nameEn

    fun category(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) categoryHi else categoryEn

    fun identificationHint(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) identificationHintHi else identificationHintEn

    fun basicCareGuide(language: AppLanguage): String =
        if (language == AppLanguage.HINDI) basicCareGuideHi else basicCareGuideEn
}

object CropDiseaseGuideProvider {

    val allGuides: List<CropDiseaseGuide> = listOf(
        CropDiseaseGuide(
            cropId = "wheat",
            imageResId = R.drawable.img_demo_wheat_rust,
            nameHi = "गेहूँ",
            nameEn = "Wheat",
            categoryHi = "रबी अनाज",
            categoryEn = "Rabi Cereal",
            identificationHintHi = "सँकरी, लंबी घास जैसी समानांतर नसों वाली पत्तियाँ और खोखला सीधा तना; पकने पर सुनहरी बालियाँ।",
            identificationHintEn = "Narrow, long grass-like leaves with parallel veins, upright hollow stems, and bearded grain spikes.",
            basicCareGuideHi = "नवंबर में समय पर बुवाई करें; पहली सिंचाई 20–25 दिन (CRI अवस्था) पर दें और यूरिया व पोटाश संतुलित रखें।",
            basicCareGuideEn = "Sow timely in November; give critical crown-root irrigation at 20–25 days and balance nitrogen with potash.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "पत्ती रतुआ / भूरा रतुआ (Leaf Rust)",
                    titleEn = "Leaf Rust (Brown Rust)",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती पर नारंगी-भूरे रंग के छोटे पाउडर जैसे उभरे दाने।",
                    symptomEn = "Small scattered orange-brown powdery pustules on the leaf blade.",
                    detailedSymptomsHi = listOf(
                        "ऊपरी पत्ती की सतह पर बिखरे हुए गोल या अंडाकार लाल-भूरे फफोले दिखते हैं।",
                        "उँगली से छूने पर जंग जैसा नारंगी-भूरा चूर्ण हाथ पर लग जाता है।",
                        "रोग बढ़ने पर पत्तियाँ समय से पहले पीली पड़कर सूखने लगती हैं और दाने पतले रह जाते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Scattered round-to-oval orange-brown pustules appear mostly on the upper leaf surface.",
                        "Rubbing an affected leaf leaves fine rust-colored powder on fingertips.",
                        "Severe infection turns leaves yellow early, causing shriveled wheat grains."
                    ),
                    favorableWeatherHi = "15–25°C तापमान, सुबह की ओस व हल्की नमी",
                    favorableWeatherEn = "Mild 15–25°C temps with morning dew and humidity",
                    tipHi = "रतुआ-प्रतिरोधी किस्म बोएं और अधिक यूरिया से बचें; दिखने पर KVK से सलाह लें।",
                    tipEn = "Grow rust-resistant varieties, avoid excess nitrogen, and consult KVK early."
                ),
                CropDiseaseEntry(
                    titleHi = "पीला रतुआ / धारीदार रतुआ (Yellow Stripe Rust)",
                    titleEn = "Yellow Stripe Rust",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों की नसों के बीच चमकीले पीले रंग की लंबी धारियाँ।",
                    symptomEn = "Bright yellow powdery stripes arranged in rows along leaf veins.",
                    detailedSymptomsHi = listOf(
                        "पत्ती की लंबाई में समानांतर पंक्तियों जैसे चमकीले पीले पाउडर के दाने बनते हैं।",
                        "अक्सर खेत में छोटे गोल धब्बों (Foci) से शुरू होकर ठंडी हवा के साथ तेजी से फैलता है।",
                        "बाद की अवस्था में पूरी पत्ती पीली-काली होकर झुलसी हुई दिखती है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Bright yellow pustules line up in narrow parallel stripes between leaf veins.",
                        "Often starts in localized field patches and spreads quickly in cool breezes.",
                        "Advanced infections scorch entire leaf blades and weaken grain filling."
                    ),
                    favorableWeatherHi = "10–18°C ठंडा मौसम, कोहरा व बारिश",
                    favorableWeatherEn = "Cool 10–18°C weather, fog, and winter rain",
                    tipHi = "ठंड व नमी में रोज खेत देखें और कृषि अधिकारी के बताए अनुसार ही उपचार करें।",
                    tipEn = "Monitor closely in cool, moist weather and follow agriculture officer advice."
                ),
                CropDiseaseEntry(
                    titleHi = "चूर्णिल आसिता / सफेद पाउडर (Powdery Mildew)",
                    titleEn = "Powdery Mildew",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती व तने पर सफेद आटे जैसी पाउडर की परत जमना।",
                    symptomEn = "White flour-like powdery fungal patches on leaves and lower stems.",
                    detailedSymptomsHi = listOf(
                        "निचली पत्तियों और पत्ती की म्यान पर सफेद-ग्रे रंग की रूई या पाउडर जैसी परत बनती है।",
                        "परत के नीचे की पत्ती पीली पड़ती है और बाद में काले छोटे बिंदु दिखाई देते हैं।",
                        "घनी बुवाई और छायादार किनारों वाले खेतों में यह जल्दी दिखता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Fluffy white-to-gray powdery patches cover lower leaf blades and sheaths.",
                        "Tissue beneath patches turns yellow with tiny dark specks in late stages.",
                        "Most visible in dense canopies and shaded field borders."
                    ),
                    favorableWeatherHi = "ठंडा, बादलों वाला व नमी युक्त मौसम",
                    favorableWeatherEn = "Cool, cloudy, and humid conditions",
                    tipHi = "घनी बुवाई व अतिरिक्त नाइट्रोजन से बचें; हवा का संचार बनाए रखें।",
                    tipEn = "Avoid overly dense seeding and excess nitrogen to keep canopy airflow."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "rice",
            imageResId = R.drawable.img_demo_healthy_rice,
            nameHi = "धान (चावल)",
            nameEn = "Rice (Paddy)",
            categoryHi = "खरीफ अनाज",
            categoryEn = "Kharif Cereal",
            identificationHintHi = "तेज किनारों वाली लंबी हरी पत्तियाँ, तने से जुड़ी पत्ती की म्यान (Leaf Sheath) और झुके हुए धान के गुच्छे।",
            identificationHintEn = "Long flat blades with slightly rough margins, clasping leaf sheaths, and drooping grain panicles.",
            basicCareGuideHi = "खेत में 2–5 सेमी हल्का पानी बनाए रखें (अतिरिक्त जल जमाव न होने दें); रोपाई में पौधे से पौधे की दूरी उचित रखें।",
            basicCareGuideEn = "Maintain shallow 2–5 cm standing water during vegetative growth with periodic drainage and proper spacing.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "झोंका रोग (Rice Blast)",
                    titleEn = "Rice Blast",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती पर आँख या हीरे के आकार के भूरे किनारे वाले धब्बे।",
                    symptomEn = "Spindle- or diamond-shaped spots with gray centers and brown margins.",
                    detailedSymptomsHi = listOf(
                        "पत्तियों पर बीच से सलेटी-सफेद और किनारों से गहरे भूरे नाव/आँख जैसे धब्बे बनते हैं।",
                        "गंभीर स्थिति में धब्बे आपस में मिलकर पूरी पत्ती को झुलसा कर सुखा देते हैं।",
                        "बालियों के नीचे गर्दन (Neck Blast) पर काला दाग पड़ने से बाली खाली सफेद रह जाती है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Diamond- or spindle-shaped lesions with pale gray centers and reddish-brown borders.",
                        "Multiple spots merge to wither and dry whole leaf blades in severe outbreaks.",
                        "Dark rotting at the panicle neck causes empty white chaffy heads (Neck Blast)."
                    ),
                    favorableWeatherHi = "रात में भारी ओस, बादल व 85%+ नमी",
                    favorableWeatherEn = "Heavy night dew, overcast skies, and 85%+ humidity",
                    tipHi = "संतुलित खाद दें, बहुत अधिक यूरिया न डालें और मेड़ें साफ रखें।",
                    tipEn = "Avoid excessive nitrogen, maintain proper spacing, and keep bunds weed-free."
                ),
                CropDiseaseEntry(
                    titleHi = "जीवाणु झुलसा (Bacterial Leaf Blight)",
                    titleEn = "Bacterial Leaf Blight",
                    pathogenType = DiseasePathogenType.BACTERIAL,
                    symptomHi = "पत्ती के किनारों से नीचे की ओर पीली-सफेद सूखती धारियाँ।",
                    symptomEn = "Water-soaked yellowing stripes starting along leaf tips and margins.",
                    detailedSymptomsHi = listOf(
                        "पत्ती की नोक और किनारों पर लहरदार जल-भीगी हरी-पीली धारियाँ शुरू होती हैं।",
                        "सुबह की नमी में किनारों पर दूधिया चिपचिपी बूंदें दिख सकती हैं जो सूखकर मोती जैसी लगती हैं।",
                        "धारियाँ पुआल जैसी सफेद-भूरे रंग की होकर ऊपर से नीचे की ओर सूख जाती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Wavy water-soaked yellow-green stripes spread downward from leaf tips and edges.",
                        "Milky bacterial ooze droplets may appear on young lesions in early morning dew.",
                        "Affected stripes turn pale straw-white and dry inward along the blade."
                    ),
                    favorableWeatherHi = "तेज हवा के साथ बारिश व 25–34°C तापमान",
                    favorableWeatherEn = "Warm 25–34°C temps with driving rain and winds",
                    tipHi = "खेत में जल जमाव कम करें और संक्रमित खेत से पानी दूसरे खेत में न जाने दें।",
                    tipEn = "Drain excess standing water and avoid draining runoff into healthy fields."
                ),
                CropDiseaseEntry(
                    titleHi = "भूरा धब्बा रोग (Brown Spot)",
                    titleEn = "Brown Spot",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती पर छोटे गोल तिल जैसे गहरे भूरे धब्बे और पीला घेरा।",
                    symptomEn = "Numerous oval dark-brown sesame-seed spots with faint yellow halos.",
                    detailedSymptomsHi = listOf(
                        "पत्ती की सतह पर बिखरे हुए गोल या अंडाकार गहरे भूरे धब्बे (तिल के दाने जैसे) दिखते हैं।",
                        "बड़े धब्बों के बीच का हिस्सा हल्का सलेटी-भूरा और बाहर हल्का पीला घेरा रहता है।",
                        "दाने व छिलके पर भी काले-भूरे दाग पड़ सकते हैं; यह पोटाश/पोषक कमी में अधिक होता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Oval dark-brown lesions resembling sesame seeds scatter evenly across leaves.",
                        "Older spots develop light gray centers surrounded by a narrow yellow halo.",
                        "Also spots grain hulls and worsens in nutrient-deficient or drought-stressed soils."
                    ),
                    favorableWeatherHi = "नमी के साथ पोषक तत्व (पोटाश/जिंक) की कमी",
                    favorableWeatherEn = "Humid weather paired with poor soil fertility",
                    tipHi = "पोटाश व जिंक की पूर्ति करें, स्वस्थ उपचारित बीज बोएं और सिंचाई नियमित रखें।",
                    tipEn = "Apply balanced potassium and zinc, use treated seed, and avoid water stress."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "tomato",
            imageResId = R.drawable.img_demo_tomato_blight,
            nameHi = "टमाटर",
            nameEn = "Tomato",
            categoryHi = "सब्ज़ी",
            categoryEn = "Vegetable",
            identificationHintHi = "दांतेदार किनारों वाली संयुक्त (Compound) पत्तियाँ, हल्के रोएँ वाला तना और छोटे पीले फूल।",
            identificationHintEn = "Serrated compound leaves with fuzzy aromatic stems and small bright-yellow star-shaped flowers.",
            basicCareGuideHi = "बांस/तार का सहारा (Staking) दें, जड़ के पास सुबह सिंचाई करें और मिट्टी छूने वाली निचली पत्तियाँ हटाएँ।",
            basicCareGuideEn = "Stake plants for airflow, water at the base in the morning, and prune lower leaves touching damp soil.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "अगेती झुलसा (Early Blight)",
                    titleEn = "Early Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "निचली पत्तियों पर गोल भूरे छल्लेदार धब्बे और हल्का पीला घेरा।",
                    symptomEn = "Dark brown concentric target-ring spots with a yellow halo on older leaves.",
                    detailedSymptomsHi = listOf(
                        "सबसे पहले जमीन के पास वाली पुरानी निचली पत्तियों पर काले-भूरे गोल धब्बे आते हैं।",
                        "धब्बों के अंदर निशाने (Target Board) जैसे गोल छल्ले साफ दिखते हैं और चारों ओर पीलापन रहता है।",
                        "रोग बढ़ने पर पत्तियाँ पीली होकर नीचे से ऊपर की ओर झड़ने लगती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Starts on older lower leaves as dark brown circular spots.",
                        "Spots show distinctive concentric target-board rings bordered by a yellow halo.",
                        "Lower foliage turns yellow and drops progressively upward."
                    ),
                    favorableWeatherHi = "गर्म नमी वाला मौसम (24–29°C) व लगातार बारिश/ओस",
                    favorableWeatherEn = "Warm 24–29°C temperatures with alternating wet-dry spells",
                    tipHi = "संक्रमित निचली पत्तियाँ हटाएँ और शाम को ऊपर से सिंचाई से बचें।",
                    tipEn = "Remove heavily infected lower leaves and avoid evening overhead watering."
                ),
                CropDiseaseEntry(
                    titleHi = "पत्ती मरोड़ वायरस (Tomato Leaf Curl)",
                    titleEn = "Tomato Leaf Curl Virus",
                    pathogenType = DiseasePathogenType.VIRAL,
                    symptomHi = "नई पत्तियाँ ऊपर की ओर मुड़ना, सिकुड़ना व पौधे का बौना रहना।",
                    symptomEn = "Upward curling, puckering, yellowing leaf edges, and stunted plant growth.",
                    detailedSymptomsHi = listOf(
                        "ऊपरी नई पत्तियाँ छोटी होकर नाव की तरह ऊपर की ओर मुड़ जाती हैं और मोटी/खुरदरी लगती हैं।",
                        "पत्ती के किनारे पीले पड़ जाते हैं और शिराओं के बीच का भाग उभरा हुआ सिकुड़ता है।",
                        "पौधे झाड़ी जैसे बौने रह जाते हैं तथा फूल गिरने से फल बहुत कम लगते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Young upper leaves curl tightly upward into cup shapes and feel leathery.",
                        "Leaf margins turn pale yellow with crinkled, puckered tissue between veins.",
                        "Plants stay stunted and bushy with heavy flower drop and poor fruit set."
                    ),
                    favorableWeatherHi = "गर्म व सूखा मौसम जब सफेद मक्खी बढ़ती है",
                    favorableWeatherEn = "Warm dry conditions that favor whitefly vectors",
                    tipHi = "सफेद मक्खी (रस चूसक कीट) की रोकथाम व रोगग्रस्त पौधे समय पर निकालना जरूरी है।",
                    tipEn = "Manage whitefly vectors early and rogue severely affected plants promptly."
                ),
                CropDiseaseEntry(
                    titleHi = "पछेती झुलसा (Late Blight)",
                    titleEn = "Late Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती पर तेजी से फैलते काले-हरे पानी से भीगे धब्बे और सफेद फफूँद।",
                    symptomEn = "Rapidly expanding dark water-soaked blotches with white mold underneath.",
                    detailedSymptomsHi = listOf(
                        "पत्ती के किनारों या बीच में अनियमित आकार के गहरे हरे-काले गीले धब्बे तुरंत फैलते हैं।",
                        "सुबह की नमी में पत्ती की निचली सतह पर धब्बों के किनारे सफेद फफूँद की परत दिखती है।",
                        "तने और हरे फलों पर भी गहरे भूरे-काले कड़े सख्त धब्बे बन सकते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Irregular greenish-black water-soaked patches expand rapidly across leaves.",
                        "Fuzzy white mold ring appears on leaf undersides in damp mornings.",
                        "Dark greasy brown lesions can also strike stems and green fruits."
                    ),
                    favorableWeatherHi = "ठंडा, कोहरेदार व बहुत अधिक नमी वाला मौसम",
                    favorableWeatherEn = "Cool, foggy, and persistently wet weather",
                    tipHi = "खेत में जल निकासी रखें, पौधों के बीच हवा का रास्ता रखें और तुरंत KVK सलाह लें।",
                    tipEn = "Ensure fast drainage, wide spacing, and consult local KVK immediately at onset."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "potato",
            imageResId = R.drawable.img_crop_potato,
            nameHi = "आलू",
            nameEn = "Potato",
            categoryHi = "सब्ज़ी",
            categoryEn = "Vegetable",
            identificationHintHi = "मेड़ों पर उगने वाला मुलायम हरा पौधा जिसमें अंडाकार छोटी पत्तियों के जोड़े (Leaflets) और सफेद/बैंगनी फूल होते हैं।",
            identificationHintEn = "Leafy mounded plants on ridges with pinnately compound oval leaflets and white or purplish flowers.",
            basicCareGuideHi = "25–30 दिन पर मिट्टी चढ़ाएँ (Earthing up); खेत में पानी भरने न दें और कोहरे में रोज पत्तियाँ जांचें।",
            basicCareGuideEn = "Earth up soil ridges at 25–30 days, ensure well-drained soil, and inspect leaves daily in foggy weather.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "पछेती झुलसा (Late Blight)",
                    titleEn = "Late Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती के किनारों पर गीले काले-भूरे धब्बे और नीचे सफेद फफूँद।",
                    symptomEn = "Dark water-soaked blotches on leaf margins with white mold underneath in damp weather.",
                    detailedSymptomsHi = listOf(
                        "पत्ती के किनारों या नोक से गहरे हरे-काले पानी से भीगे धब्बे शुरू होते हैं।",
                        "नमी वाली सुबह में धब्बे के नीचे किनारों पर सफेद मखमली फफूँद दिखती है।",
                        "रोग उग्र होने पर पूरा खेत कुछ ही दिनों में काला पड़कर सड़न की गंध देने लगता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Pale-green to dark brown water-soaked blotches start at leaf tips or edges.",
                        "White cottony fungal growth rings the underside margin during morning dew.",
                        "Severe outbreaks blacken whole foliage canopies within days with a musty odor."
                    ),
                    favorableWeatherHi = "10–20°C ठंड, घना कोहरा और 80%+ नमी",
                    favorableWeatherEn = "Cool 10–20°C temps, dense fog, and 80%+ humidity",
                    tipHi = "कोहरे व नमी में तुरंत खेत की जांच करें और विशेषज्ञ की सलाह से सुरक्षा उपाय करें।",
                    tipEn = "Inspect daily during foggy humid weather and consult local experts immediately."
                ),
                CropDiseaseEntry(
                    titleHi = "अगेती झुलसा (Early Blight)",
                    titleEn = "Early Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों पर छोटे गोल गहरे भूरे छल्लेदार धब्बे।",
                    symptomEn = "Circular dark brown lesions with concentric rings on mature leaves.",
                    detailedSymptomsHi = listOf(
                        "निचली पुरानी पत्तियों पर छोटे, कोणीय से गोल गहरे भूरे धब्बे (Bull's-eye rings) बनते हैं।",
                        "धब्बों की बढ़वार अक्सर पत्ती की नसों से सीमित रहती है और किनारे पीले पड़ते हैं।",
                        "पौधे के कमजोर होने पर ऊपरी पत्तियाँ भी सूखकर झड़ जाती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Small dark-brown lesions with concentric rings form first on older lower leaves.",
                        "Spots are often bounded by main veins and bordered by yellowing tissue.",
                        "Defoliation accelerates if plants suffer nitrogen or moisture stress."
                    ),
                    favorableWeatherHi = "24–30°C तापमान व रुक-रुक कर होने वाली नमी",
                    favorableWeatherEn = "Warm 24–30°C weather with intermittent dew",
                    tipHi = "रोग-मुक्त बीज कंद चुनें और हर साल फसल चक्र अपनाएँ।",
                    tipEn = "Plant certified disease-free seed tubers and practice crop rotation."
                ),
                CropDiseaseEntry(
                    titleHi = "पत्ती मरोड़ / मोज़ेक वायरस (Potato Leaf Roll & Mosaic)",
                    titleEn = "Potato Leaf Roll & Mosaic Virus",
                    pathogenType = DiseasePathogenType.VIRAL,
                    symptomHi = "पत्तियों का ऊपर मुड़ना, कड़क होना या हल्के-गहरे हरे चकत्ते पड़ना।",
                    symptomEn = "Upward rolling of stiff leaves or faint yellow-green mottling patterns.",
                    detailedSymptomsHi = listOf(
                        "निचली या ऊपरी पत्तियाँ किनारे से अंदर की ओर गोल मुड़ती हैं और छूने पर कागज जैसी कड़क (Brittle) लगती हैं।",
                        "मोज़ेक में पत्तियों पर हल्के पीले व गहरे हरे रंग की चित्तीदार आकृतियाँ दिखती हैं।",
                        "पौधे का आकार छोटा रहता है और कंद (आलू) छोटे बनते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Leaf margins roll inward into tubes and feel stiff or leathery when squeezed.",
                        "Mosaic strains create patchy light-green and yellow mottling across leaflets.",
                        "Infected plants remain stunted and yield noticeably smaller tubers."
                    ),
                    favorableWeatherHi = "माहू (Aphids) की सक्रियता वाला हल्का मौसम",
                    favorableWeatherEn = "Mild weather when aphid vectors are active",
                    tipHi = "माहू (Aphid) की निगरानी करें और रोगग्रस्त पौधे व कंद बीज के लिए न रखें।",
                    tipEn = "Control aphid vectors and never save seed tubers from virus-affected plants."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "cotton",
            imageResId = R.drawable.img_crop_cotton,
            nameHi = "कपास",
            nameEn = "Cotton",
            categoryHi = "नकदी फसल",
            categoryEn = "Cash Crop",
            identificationHintHi = "चौड़ी हथेली जैसे 3–5 कटावदार पत्तों वाला झाड़ीदार पौधा, क्रीम-पीले फूल और खुलने वाले सफेद टिंडे (Bolls)।",
            identificationHintEn = "Bushy stems with broad 3- to 5-lobed palmate green leaves, cream-yellow blossoms, and white lint bolls.",
            basicCareGuideHi = "अतिरिक्त सिंचाई से वानस्पतिक बढ़वार रोकें, खरपतवार नियंत्रण रखें और रसचूसक कीटों के लिए पीले चिपचिपे ट्रैप लगाएँ।",
            basicCareGuideEn = "Avoid excessive irrigation, keep rows weed-free, and monitor sucking pests using yellow sticky traps.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "पत्ती मरोड़ रोग (Cotton Leaf Curl)",
                    titleEn = "Cotton Leaf Curl",
                    pathogenType = DiseasePathogenType.VIRAL,
                    symptomHi = "पत्तियों का प्याले जैसा मुड़ना और नसों का मोटा हरा दिखना।",
                    symptomEn = "Upward or downward cupping of leaves with thickened dark-green veins.",
                    detailedSymptomsHi = listOf(
                        "नई पत्तियाँ ऊपर या नीचे की ओर प्याले जैसा आकार लेकर मुड़ जाती हैं।",
                        "पत्ती के निचले हिस्से को सूरज की तरफ देखने पर नसें (Veins) मोटी व गहरी हरी उभरी दिखती हैं।",
                        "कभी-कभी पत्ती के नीचे छोटी पत्ती जैसी संरचना (Enation) निकल आती है और टिंडे कम बनते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Young leaves cup upward or downward and pucker along the edges.",
                        "Veins on leaf undersides thicken and turn dark green when held toward light.",
                        "Small leaf-like outgrowths (enations) may form under veins, cutting boll yield."
                    ),
                    favorableWeatherHi = "गर्म व उमस भरा मौसम जब सफेद मक्खी पनपती है",
                    favorableWeatherEn = "Warm weather supporting whitefly population buildup",
                    tipHi = "सफेद मक्खी पर नियंत्रण रखें और सहनशील संकर किस्में लगाएँ।",
                    tipEn = "Monitor whiteflies early and choose tolerant varieties recommended locally."
                ),
                CropDiseaseEntry(
                    titleHi = "जीवाणु कोणीय धब्बा (Angular Leaf Spot)",
                    titleEn = "Bacterial Angular Leaf Spot",
                    pathogenType = DiseasePathogenType.BACTERIAL,
                    symptomHi = "पत्ती पर नसों से घिरे कोणीय गहरे भूरे-काले धब्बे।",
                    symptomEn = "Angular water-soaked dark brown spots bounded by leaf veins.",
                    detailedSymptomsHi = listOf(
                        "पत्ती की निचली सतह पर पहले छोटे, चौकोर या त्रिकोणीय जल-भीगे हरे धब्बे दिखते हैं।",
                        "नसों के बीच सीमित रहने के कारण धब्बों का आकार कोणीय (Angular) होता है जो बाद में काला पड़ जाता है।",
                        "गंभीर स्थिति में तने पर काली धारियाँ (Black Arm) और टिंडों पर धंसे हुए काले दाग पड़ते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Small dark-green water-soaked spots bounded sharply by minor leaf veins.",
                        "Lesions turn dark brown to black and give infected leaves a ragged, tattered look.",
                        "Can spread to stems (Black Arm) and cause sunken dark spots on cotton bolls."
                    ),
                    favorableWeatherHi = "तेज बारिश की बौछारें व 28–35°C गर्मी",
                    favorableWeatherEn = "Wind-driven rain splashes and warm 28–35°C weather",
                    tipHi = "प्रमाणित उपचारित बीज बोएं और खेत में जलभराव न होने दें।",
                    tipEn = "Use acid-delinted certified seed and ensure good field drainage."
                ),
                CropDiseaseEntry(
                    titleHi = "दहिया / ग्रे मिल्ड्यू (Grey Mildew)",
                    titleEn = "Grey Mildew (Dahiya)",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती के निचले भाग पर सफ़ेद दही या फ्रॉस्ट जैसे कोणीय धब्बे।",
                    symptomEn = "White frosted or curd-like angular patches on lower leaf surfaces.",
                    detailedSymptomsHi = listOf(
                        "निचली पुरानी पत्तियों के निचले हिस्से पर चमकीले सफेद-चाँदी जैसे पाउडर धब्बे बनते हैं।",
                        "ऊपरी सतह से देखने पर वे स्थान पीले-भूरे दिखाई देते हैं।",
                        "रोग फैलने पर पत्तियाँ समय से पहले लाल-भूरी होकर झड़ने लगती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Frosty white angular fungal patches coat the underside of mature lower leaves.",
                        "Corresponding areas on the upper leaf surface turn pale yellow to reddish-brown.",
                        "Severe infection causes early leaf shedding before bolls finish opening."
                    ),
                    favorableWeatherHi = "अधिक नमी, घनी फसल व देर की बारिश",
                    favorableWeatherEn = "High humidity, dense canopy, and late-season rain",
                    tipHi = "खरपतवार हटाएँ, संतुलित खाद दें और फसल गिरने से बचाएँ।",
                    tipEn = "Clear weeds, avoid excess nitrogen, and inspect lower canopy after late rains."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "sugarcane",
            imageResId = R.drawable.img_crop_sugarcane,
            nameHi = "गन्ना",
            nameEn = "Sugarcane",
            categoryHi = "नकदी फसल",
            categoryEn = "Cash Crop",
            identificationHintHi = "गाँठदार मोटे हरे-पीले तने (Pori) और ऊपर की ओर तलवार जैसी लंबी झुकती पत्तियाँ।",
            identificationHintEn = "Tall jointed canes with distinct nodes and long arching sword-shaped green leaves.",
            basicCareGuideHi = "स्वस्थ 2–3 आँख वाले टुकड़े बोएं, कल्ले निकलने पर मिट्टी चढ़ाएँ और सूखी नीचे की पत्तियाँ मल्च के रूप में बिछाएँ।",
            basicCareGuideEn = "Plant healthy 2–3 bud setts, earth up during tillering, and use trashed dry lower leaves as moisture mulch.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "लाल सड़न (Red Rot)",
                    titleEn = "Red Rot",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "ऊपरी 3–4 पत्तियों का पीला पड़कर सूखना व तने के अंदर लाल रंग।",
                    symptomEn = "Yellowing and drying of upper crown leaves; reddish internal stalk discoloration.",
                    detailedSymptomsHi = listOf(
                        "ऊपरी मुकुट की तीसरी-चौथी पत्ती किनारों से पीली पड़कर मुरझाती और सूखने लगती है।",
                        "गन्ने के तने को लंबाई में चीरकर देखने पर अंदर का गूदा लाल रंग का दिखता है जिसमें सफेद आड़ी पट्टियाँ होती हैं।",
                        "चीरे हुए गन्ने से सिरके या सड़े अल्कोहल जैसी खट्टी गंध आती है और गन्ना खोखला पड़ जाता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Third and fourth leaves from the top crown yellow along margins and droop dry.",
                        "Splitting the stalk lengthwise reveals bright red internal pith crossed by white bands.",
                        "Split stalks emit a sour alcoholic or vinegar-like odor and shrivel at nodes."
                    ),
                    favorableWeatherHi = "जलभराव, भारी मानसून बारिश व लगातार पेड़ी फसल",
                    favorableWeatherEn = "Waterlogging, monsoon rains, and continuous ratoon cropping",
                    tipHi = "रोगग्रस्त थान जड़ से उखाड़ें और केवल स्वस्थ प्रमाणित बीज गन्ने लगाएँ।",
                    tipEn = "Uproot affected clumps immediately and plant only healthy seed setts."
                ),
                CropDiseaseEntry(
                    titleHi = "कंडुआ रोग / चाबुक कंड (Whip Smut)",
                    titleEn = "Sugarcane Whip Smut",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पौधे के सिरे से काले चूर्ण से लिपटा चाबुक जैसा मुड़ा सिरा निकलना।",
                    symptomEn = "Long black whip-like shoot emerging from the central growing spindle.",
                    detailedSymptomsHi = listOf(
                        "पौधे के बीच के गोभे से पतला, लंबा और मुड़ा हुआ काले चाबुक जैसा सिरा निकलता है।",
                        "शुरुआत में यह पतली सफेद झिल्ली से ढका होता है जो फटते ही काला कालिख जैसा पाउडर हवा में फैलाती है।",
                        "पौधे में पतले, घास जैसे कई छोटे कल्ले निकलते हैं और मोटा गन्ना नहीं बनता।"
                    ),
                    detailedSymptomsEn = listOf(
                        "A dusty black curved whip-like structure grows straight from the shoot apex.",
                        "Thin silvery membrane tears open to release clouds of black powdery spores.",
                        "Affected stools throw thin grassy tillers that fail to form millable canes."
                    ),
                    favorableWeatherHi = "गर्म व सूखा मौसम और संक्रमित पेड़ी (Ratoon)",
                    favorableWeatherEn = "Hot dry spells and infected ratoon stubbles",
                    tipHi = "काला सिरा झिल्ली फटने से पहले पॉलीथीन थैली में ढककर काटें व जलाएँ; पेड़ी न रखें।",
                    tipEn = "Bag and remove whips before spores scatter; avoid ratooning infected fields."
                ),
                CropDiseaseEntry(
                    titleHi = "पोक्का बोइंग / शीर्ष सड़न (Pokkah Boeng)",
                    titleEn = "Pokkah Boeng (Twisted Top)",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "नई पत्तियों के आधार का पीला पड़ना, मुड़ना व सिकुड़ी जाली बनना।",
                    symptomEn = "Chlorotic yellow base on young spindle leaves with twisting and wrinkling.",
                    detailedSymptomsHi = listOf(
                        "गोभे की सबसे नई पत्तियाँ आधार (Base) से पीली-सफेद दिखती हैं और आपस में लिपटकर मुड़ जाती हैं।",
                        "पत्तियों पर लाल-भूरे धब्बे व सीढ़ी जैसे छोटे कट (Ladder-like lesions) दिखते हैं।",
                        "गंभीर स्थिति में तने का ऊपरी सिरा सड़कर टूट जाता है (Top Rot)।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Young spindle leaves turn pale yellow at the base, wrinkle, and twist tightly.",
                        "Reddish-brown specks and small ladder-like splits appear along leaf bases.",
                        "Advanced cases cause internal top rot of the growing cane apex."
                    ),
                    favorableWeatherHi = "मानसून की पहली बारिश व तेजी से बढ़वार का समय",
                    favorableWeatherEn = "Humid early monsoon spells during rapid cane elongation",
                    tipHi = "खेत में जल निकासी सुधारें और संतुलित खाद दें; अधिकतर पौधे मौसम खुलने पर संभल जाते हैं।",
                    tipEn = "Improve field drainage and avoid heavy nitrogen surges during wet spells."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "maize",
            imageResId = R.drawable.img_crop_maize,
            nameHi = "मक्का",
            nameEn = "Maize",
            categoryHi = "खरीफ अनाज",
            categoryEn = "Kharif Cereal",
            identificationHintHi = "मजबूत एकल तना, चौड़ी लहरदार किनारे वाली लंबी पत्तियाँ, सबसे ऊपर नर मंजरी (Tassel) और तने पर भुट्टा।",
            identificationHintEn = "Sturdy solid stalk with wide wavy-edged leaves, feathery top tassel, and silked ears along the stem.",
            basicCareGuideHi = "खेत में जल निकासी उत्तम रखें (मक्का जलभराव से जल्दी प्रभावित होता है); घुटने की ऊँचाई और फूल आते समय सिंचाई जरूरी है।",
            basicCareGuideEn = "Ensure good drainage since maize dislikes waterlogging; irrigate at knee-high and tasseling stages.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "टर्सिकम पत्ती झुलसा (Turcicum Leaf Blight)",
                    titleEn = "Turcicum Leaf Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों पर नाव के आकार के लंबे सलेटी-भूरे धब्बे।",
                    symptomEn = "Long cigar- or boat-shaped grayish-brown lesions along leaves.",
                    detailedSymptomsHi = listOf(
                        "निचली पत्तियों पर पहले हल्के हरे पानी जैसे धब्बे दिखते हैं जो बढ़कर 5–15 सेमी लंबे नाव जैसे हो जाते हैं।",
                        "धब्बों का रंग सलेटी-हरा से हल्का भूरा होता है और नमी में बीच में काली फफूँद दिखती है।",
                        "गंभीर रोग में पत्तियाँ पाले से जली हुई दिखती हैं और भुट्टे छोटे रह जाते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Elongated 5–15 cm cigar-shaped grayish-green lesions develop first on lower leaves.",
                        "Lesions turn tan-brown with dark sooty spore dust in the center when humid.",
                        "Severe blight leaves plants looking frost-scorched, cutting cob fill."
                    ),
                    favorableWeatherHi = "18–27°C तापमान, भारी ओस व लगातार बारिश",
                    favorableWeatherEn = "Moderate 18–27°C weather with heavy dew and rain",
                    tipHi = "अधिक नमी वाले क्षेत्रों में प्रतिरोधी किस्में बोएं और फसल अवशेष नष्ट करें।",
                    tipEn = "Sow resistant hybrids and plow under infected crop residue after harvest."
                ),
                CropDiseaseEntry(
                    titleHi = "पत्ती व आवरण झुलसा (Banded Leaf & Sheath Blight)",
                    titleEn = "Banded Leaf & Sheath Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती की म्यान व निचली पत्तियों पर साँप की केंचुली जैसी आड़ी पट्टियाँ।",
                    symptomEn = "Large alternating bleached-white and brown bands on sheaths and lower leaves.",
                    detailedSymptomsHi = listOf(
                        "जमीन के पास तने की म्यान (Leaf Sheath) पर बड़े हल्के सफेद व गहरे भूरे किनारों वाले धब्बे बनते हैं।",
                        "धब्बे लहरदार पट्टियों (Snakeskin pattern) जैसे ऊपर पत्तियों और भुट्टे के छिलके तक चढ़ते हैं।",
                        "नमी में धब्बों पर सफेद रूई जैसा जाल और सरसों के दाने जैसी भूरी गांठें (Sclerotia) दिखती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Concentric bleached-white and dark-brown bands form a snakeskin pattern on lower sheaths.",
                        "Bands climb upward onto leaf blades and outer cob husks in humid weather.",
                        "White cottony threads and mustard-seed-like brown sclerotia appear on rotting husks."
                    ),
                    favorableWeatherHi = "28–32°C गर्मी, घना खेत व लगातार नमी",
                    favorableWeatherEn = "Warm 28–32°C weather, dense stands, and prolonged dampness",
                    tipHi = "नीचे की 2–3 संक्रमित पत्तियाँ हटाएँ (Stripping) और मेड़ों पर खरपतवार साफ रखें।",
                    tipEn = "Strip lowest 2–3 infected leaves early and keep field borders weed-free."
                ),
                CropDiseaseEntry(
                    titleHi = "मेडिस पत्ती धब्बा (Maydis Leaf Blight)",
                    titleEn = "Maydis (Southern) Leaf Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों पर छोटे आयताकार पीले-भूरे धब्बे जो नसों के बीच रहते हैं।",
                    symptomEn = "Small oval to rectangular tan lesions bounded by parallel leaf veins.",
                    detailedSymptomsHi = listOf(
                        "पत्ती पर 1–3 सेमी के छोटे पीले-भूरे धब्बे बनते हैं जिनके किनारे हल्के लाल-भूरे होते हैं।",
                        "धब्बे पत्तियों की समानांतर नसों के बीच सीमित रहकर आयताकार (Rectangular) दिखते हैं।",
                        "टर्सिकम झुलसा की तुलना में ये छोटे लेकिन संख्या में बहुत अधिक होते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Numerous small 1–3 cm tan-brown lesions with darker reddish-brown borders.",
                        "Parallel leaf veins restrict lesion width, giving a boxy or oval outline.",
                        "Heavy spotting merges to dry out lower and middle canopy leaves."
                    ),
                    favorableWeatherHi = "25–32°C गर्म व नमी भरा खरीफ मौसम",
                    favorableWeatherEn = "Warm 25–32°C humid monsoon conditions",
                    tipHi = "संतुलित उर्वरक दें, फसल चक्र अपनाएँ और सहनशील संकर किस्में चुनें।",
                    tipEn = "Rotate crops with pulses/oilseeds and select locally resistant hybrids."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "soybean",
            imageResId = R.drawable.img_crop_soybean,
            nameHi = "सोयाबीन",
            nameEn = "Soybean",
            categoryHi = "तिलहन / दलहन",
            categoryEn = "Oilseed / Pulse",
            identificationHintHi = "एक डंठल पर तीन अंडाकार हरी पत्तियाँ (Trifoliate) और तने पर छोटे रोएँदार फलियों के गुच्छे।",
            identificationHintEn = "Bushy plants with three oval leaflets per stem (trifoliate) and clusters of fuzzy green seed pods.",
            basicCareGuideHi = "शुरुआती 30–40 दिन निराई-गुड़ाई कर खेत साफ रखें; फली भरने के समय सूखा पड़ने पर हल्की सिंचाई दें।",
            basicCareGuideEn = "Keep fields weed-free during the first 30–40 days and give supplemental water if dry during pod filling.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "पीला मोज़ेक रोग (Yellow Mosaic)",
                    titleEn = "Soybean Yellow Mosaic",
                    pathogenType = DiseasePathogenType.VIRAL,
                    symptomHi = "पत्तियों पर पीले और हरे रंग के चित्तीदार धब्बे, बाद में पूरी पत्ती पीली।",
                    symptomEn = "Bright yellow and green mottled patches spreading across young leaves.",
                    detailedSymptomsHi = listOf(
                        "ऊपरी नई पत्तियों पर पहले छोटे चमकीले पीले धब्बे बिखरते हैं जो हरे रंग के साथ चित्तीदार दिखते हैं।",
                        "बाद में पूरी पत्ती पीली पड़कर किनारों से थोड़ा मुड़ जाती है व पत्तियों पर झुर्रियाँ पड़ती हैं।",
                        "रोगग्रस्त पौधों में फलियाँ बहुत कम बनती हैं और दाने छोटे व सिकुड़े रह जाते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Bright yellow spots scatter across young trifoliate leaves, creating a yellow-green mosaic.",
                        "Affected leaves gradually turn mostly golden-yellow and slightly puckered.",
                        "Early-infected plants produce few pods with small, shriveled seeds."
                    ),
                    favorableWeatherHi = "गर्म व बीच-बीच में सूखा मौसम (सफेद मक्खी प्रकोप)",
                    favorableWeatherEn = "Warm spells with dry breaks favoring whitefly buildup",
                    tipHi = "शुरुआती रोगग्रस्त पौधे हटाएँ और सफेद मक्खी की निगरानी करें।",
                    tipEn = "Remove early infected plants and monitor whitefly population."
                ),
                CropDiseaseEntry(
                    titleHi = "सोयाबीन रतुआ (Soybean Rust)",
                    titleEn = "Soybean Rust",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती की निचली सतह पर हल्के भूरे उभरे दाने और ऊपरी सतह पर धब्बे।",
                    symptomEn = "Tiny tan-to-reddish-brown pustules clustered on the lower leaf surface.",
                    detailedSymptomsHi = listOf(
                        "निचली व मध्य पत्तियों पर पहले छोटे पीले-भूरे कोणीय धब्बे दिखते हैं।",
                        "पत्ती पलटकर देखने पर निचली सतह पर हल्के भूरे रंग के उभरे फफोले (Volcano-like pustules) मिलते हैं।",
                        "पत्तियाँ तेजी से पीली पड़कर समय से पहले झड़ जाती हैं जिससे फली खाली रह जाती है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Small polygonal gray-green to reddish-brown specks appear on lower leaves.",
                        "Leaf undersides show tiny raised tan pustules releasing dusty rust spores.",
                        "Rapid premature defoliation leaves pods unfilled."
                    ),
                    favorableWeatherHi = "18–28°C व लगातार 6+ घंटे पत्ती गीली रहना",
                    favorableWeatherEn = "Prolonged leaf wetness and 18–28°C humid weather",
                    tipHi = "फूल व फली बनने के समय निचली पत्तियाँ पलटकर जांचें और समय पर KVK सलाह लें।",
                    tipEn = "Inspect undersides of lower leaves at flowering and consult KVK promptly."
                ),
                CropDiseaseEntry(
                    titleHi = "एन्थ्रेक्नोज / फली झुलसा (Anthracnose & Pod Blight)",
                    titleEn = "Anthracnose & Pod Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "तने, डंठल व फलियों पर गहरे भूरे-काले धंसे हुए धब्बे।",
                    symptomEn = "Dark sunken brown-to-black lesions on stems, petioles, and seed pods.",
                    detailedSymptomsHi = listOf(
                        "पत्ती के डंठल (Petioles) और तने पर अनियमित गहरे भूरे-काले धब्बे बनते हैं जिनसे पत्तियाँ लटक जाती हैं।",
                        "फलियों पर काले धंसे हुए धब्बे बनते हैं जिनमें छोटे काले काँटे जैसे बिंदु समूह में दिखते हैं।",
                        "संक्रमित फलियों के दाने फफूँद लगकर खराब हो जाते हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Irregular dark brown sunken lesions girdle petioles and stems, causing leaf droop.",
                        "Pods show dark blotches dotted with tiny black pinhead fruiting bodies.",
                        "Seeds inside blighted pods turn moldy and discolored."
                    ),
                    favorableWeatherHi = "फली बनते समय गर्म व भारी बारिश वाला मौसम",
                    favorableWeatherEn = "Warm wet weather during pod development",
                    tipHi = "स्वस्थ बीज से बुवाई करें, खेत में जलभराव न हो और फसल चक्र अपनाएँ।",
                    tipEn = "Plant clean treated seed, ensure drainage, and rotate away from soybean."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "mustard",
            imageResId = R.drawable.img_crop_mustard,
            nameHi = "सरसों",
            nameEn = "Mustard",
            categoryHi = "रबी तिलहन",
            categoryEn = "Rabi Oilseed",
            identificationHintHi = "चमकीले पीले चार-पंखुड़ी वाले फूलों के गुच्छे, चौड़ी निचली पत्तियाँ और लंबी पतली फलियाँ (Siliqua)।",
            identificationHintEn = "Clusters of bright yellow 4-petal blossoms above broad lobed lower leaves and slender seed pods.",
            basicCareGuideHi = "अक्टूबर में बुवाई करें; 15–20 दिन पर घने पौधों की छंटाई (Thinning) करें और माहू (चेपा) के लिए पीली कलियाँ देखें।",
            basicCareGuideEn = "Sow in October, thin crowded seedlings at 15–20 days, and inspect flower buds regularly for aphids.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "सफेद रतुआ (White Rust)",
                    titleEn = "White Rust",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती की निचली सतह पर सफेद चाक जैसे उभरे फफोले।",
                    symptomEn = "Creamy-white chalky raised pustules on the underside of leaves.",
                    detailedSymptomsHi = listOf(
                        "पत्ती की निचली सतह पर चमकीले सफेद चाक या मलाई जैसे उभरे फफोले दिखते हैं।",
                        "ऊपरी सतह पर उन्हीं स्थानों के ऊपर हल्के पीले धब्बे दिखाई देते हैं।",
                        "फूल आने की अवस्था में फूल और टहनियाँ सूजकर टेढ़ी-मेढ़ी (Staghead) हो जाती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Raised creamy-white chalky blisters dot the underside of lower leaves.",
                        "Corresponding patches on the upper leaf turn pale yellow.",
                        "Systemic infection swells flower stalks into distorted green staghead galls."
                    ),
                    favorableWeatherHi = "10–18°C ठंडा, बादल व ओस भरा मौसम",
                    favorableWeatherEn = "Cool 10–18°C cloudy weather with morning dew",
                    tipHi = "समय पर बुवाई करें और खेत में खरपतवार न बढ़ने दें।",
                    tipEn = "Sow at the recommended time and keep fields clean of mustard-family weeds."
                ),
                CropDiseaseEntry(
                    titleHi = "अल्टरनेरिया धब्बा (Alternaria Blight)",
                    titleEn = "Alternaria Leaf Blight",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों व फलियों पर गोल काले-भूरे छल्लेदार धब्बे।",
                    symptomEn = "Circular dark brown spots with concentric rings on leaves and pods.",
                    detailedSymptomsHi = listOf(
                        "निचली पत्तियों पर छोटे काले बिंदु आते हैं जो बढ़कर गोल छल्लेदार (Target-ring) धब्बे बन जाते हैं।",
                        "धब्बों के चारों ओर हल्का पीला घेरा रहता है और बाद में पत्तियाँ झड़ जाती हैं।",
                        "फलियों पर गहरे गोल दाग पड़ने से फलियाँ समय से पहले सूखकर चटक जाती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Small dark dots enlarge into circular brown-black lesions with concentric rings.",
                        "Yellow halos surround spots before heavily spotted leaves turn brown and drop.",
                        "Dark circular spots on seed pods lead to premature pod splitting and small seeds."
                    ),
                    favorableWeatherHi = "हल्की बारिश, कोहरा व 70%+ नमी",
                    favorableWeatherEn = "Winter rain, fog, and >70% relative humidity",
                    tipHi = "फसल चक्र अपनाएँ और नमी बढ़ने पर कृषि विशेषज्ञ से सलाह लें।",
                    tipEn = "Follow crop rotation and seek expert advice during prolonged damp weather."
                ),
                CropDiseaseEntry(
                    titleHi = "चूर्णिल आसिता / सफेद पाउडर (Powdery Mildew)",
                    titleEn = "Powdery Mildew",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्तियों, तनों व फलियों पर सफेद पाउडर जैसी परत जमना।",
                    symptomEn = "White powdery coating spreading over leaves, stems, and developing pods.",
                    detailedSymptomsHi = listOf(
                        "फरवरी-मार्च के आसपास निचली पत्तियों पर सफेद गोल पाउडर जैसे धब्बे शुरू होते हैं।",
                        "तेजी से फैलकर पूरी पत्ती, तने और फलियों को सफेद आटे जैसी परत से ढक लेता है।",
                        "प्रभावित फलियाँ छोटी व अधपकी रह जाती हैं जिससे तेल की मात्रा घटती है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Circular white floury patches appear on both sides of leaves in late winter.",
                        "Spreads rapidly to coat stems and green siliqua pods in white powder.",
                        "Severely coated pods stay small and produce shriveled, low-oil seeds."
                    ),
                    favorableWeatherHi = "दिन में हल्की गर्मी व रात में ठंडी ओस",
                    favorableWeatherEn = "Warm days with cool nights and moderate humidity",
                    tipHi = "देर से बुवाई से बचें और समय पर सिंचाई व पोषण संतुलन रखें।",
                    tipEn = "Avoid late sowing and maintain proper irrigation during pod development."
                )
            )
        ),
        CropDiseaseGuide(
            cropId = "chilli",
            imageResId = R.drawable.img_crop_chilli,
            nameHi = "मिर्च",
            nameEn = "Chilli",
            categoryHi = "मसाला / सब्ज़ी",
            categoryEn = "Spice / Vegetable",
            identificationHintHi = "चिकनी चमकदार नुकीली अंडाकार पत्तियाँ, छोटे सफेद फूल और शाखाओं पर लटकती हरी या लाल मिर्च।",
            identificationHintEn = "Glossy smooth pointed oval leaves, small white downward-facing blossoms, and green or red chilli pods.",
            basicCareGuideHi = "मेड़ों पर रोपाई करें ताकि जड़ सड़न न हो; पत्ती के नीचे थ्रिप्स व माइट्स की नियमित जांच करें।",
            basicCareGuideEn = "Transplant on raised beds to prevent root rot and regularly check leaf undersides for thrips and mites.",
            diseases = listOf(
                CropDiseaseEntry(
                    titleHi = "पत्ती मरोड़ / कोकड़ा (Leaf Curl / Murda)",
                    titleEn = "Chilli Leaf Curl (Murda)",
                    pathogenType = DiseasePathogenType.PEST_COMPLEX,
                    symptomHi = "पत्तियों का ऊपर या नीचे मुड़कर छोटा व सिकुड़ा रह जाना।",
                    symptomEn = "Curling, crinkling, and cupping of leaves caused by thrips, mites, or virus.",
                    detailedSymptomsHi = listOf(
                        "थ्रिप्स के कारण पत्ती के किनारे नाव की तरह ऊपर की ओर मुड़ते हैं और डंठल लंबा दिखता है।",
                        "माइट्स (मकड़ी) के प्रकोप से पत्तियाँ नीचे की ओर मुड़कर गहरी हरी व चमकदार मुड़ी हुई दिखती हैं।",
                        "वायरस जुड़ने पर नई पत्तियाँ बहुत छोटी, पीली-हरी चित्तीदार व गुच्छे जैसी (Bunchy) रह जाती हैं।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Thrips feeding curls leaf edges upward like a boat with elongated petioles.",
                        "Broad mites curl leaves downward with bronzed, leathery undersides.",
                        "Viral leaf curl shrinks new shoots into yellow-mottled, stunted rosettes."
                    ),
                    favorableWeatherHi = "गर्म व सूखा मौसम जब थ्रिप्स व माइट्स बढ़ते हैं",
                    favorableWeatherEn = "Warm dry weather favoring thrips, mites, and whiteflies",
                    tipHi = "थ्रिप्स और माइट्स की जांच पत्ती पलटकर करें और KVK के अनुसार उपचार लें।",
                    tipEn = "Check leaf undersides for thrips/mites and consult KVK for targeted control."
                ),
                CropDiseaseEntry(
                    titleHi = "एन्थ्रेक्नोज / डाइबैक व फल सड़न (Anthracnose & Die-Back)",
                    titleEn = "Anthracnose & Die-Back",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "टहनी का सिरे से सूखना और पके फलों पर गोल धंसे काले धब्बे।",
                    symptomEn = "Twig die-back from tips and sunken circular dark spots on ripening pods.",
                    detailedSymptomsHi = listOf(
                        "टहनियों के ऊपरी सिरे (Tips) से गहरे भूरे होकर नीचे की ओर सूखने लगते हैं (Die-Back)।",
                        "हरी व लाल मिर्च पर गोल पानी से भीगे धंसे हुए धब्बे बनते हैं जिनमें छल्लेदार काले बिंदु होते हैं।",
                        "अधिक नमी में धब्बों के बीच हल्का गुलाबी-सफेद फफूँद समूह दिखता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Tender shoot tips turn dark brown and dry backward downward (Die-Back).",
                        "Green and red chillies develop sunken circular spots with concentric black rings.",
                        "Pinkish-orange spore masses appear inside pod lesions during humid weather."
                    ),
                    favorableWeatherHi = "उमस, भारी बारिश व 25–30°C तापमान",
                    favorableWeatherEn = "Warm 25–30°C weather with monsoon rains and splashing",
                    tipHi = "संक्रमित सूखी टहनियाँ व सड़े फल तुरंत तोड़कर खेत से दूर नष्ट करें।",
                    tipEn = "Prune blighted tips and pick off rotting pods early to stop spread."
                ),
                CropDiseaseEntry(
                    titleHi = "सर्कोस्पोरा धब्बा (Cercospora Frog-Eye Spot)",
                    titleEn = "Cercospora (Frog-Eye) Leaf Spot",
                    pathogenType = DiseasePathogenType.FUNGAL,
                    symptomHi = "पत्ती पर बीच से सफ़ेद व बाहर गहरे भूरे किनारे वाले मेंढक की आँख जैसे धब्बे।",
                    symptomEn = "Circular spots with pale gray-white centers and dark brown borders.",
                    detailedSymptomsHi = listOf(
                        "निचली पत्तियों पर छोटे गोल धब्बे बनते हैं जिनका बीच का भाग हल्का सफेद-सलेटी होता है।",
                        "बाहरी किनारा गहरा भूरा या बैंगनी-काला होने से ये मेंढक की आँख (Frog-Eye) जैसे दिखते हैं।",
                        "धब्बे बढ़ने पर पत्तियाँ पीली पड़कर झड़ जाती हैं और पौधा पत्तियों से खाली दिखता है।"
                    ),
                    detailedSymptomsEn = listOf(
                        "Round spots on lower leaves with ash-gray centers and dark brown margins.",
                        "Distinct 'frog-eye' ring appearance causes severe yellowing around clustered spots.",
                        "Leaves drop prematurely, exposing chillies to sunscald."
                    ),
                    favorableWeatherHi = "लगातार नमी, गर्म रातें व घना खेत",
                    favorableWeatherEn = "Prolonged humidity, warm nights, and dense planting",
                    tipHi = "फसल अवशेष हटाएँ, पानी जड़ के पास दें और पौधों के बीच हवा बनाए रखें।",
                    tipEn = "Remove crop debris, water at root zone, and maintain airy plant spacing."
                )
            )
        )
    )

    fun filterGuides(
        query: String,
        selectedCropId: String? = null
    ): List<CropDiseaseGuide> {
        val trimmed = query.trim().lowercase()
        val base = if (selectedCropId != null) {
            allGuides.filter { it.cropId == selectedCropId }
        } else {
            allGuides
        }
        if (trimmed.isEmpty()) return base

        return base.filter { guide ->
            guide.nameHi.lowercase().contains(trimmed) ||
                guide.nameEn.lowercase().contains(trimmed) ||
                guide.categoryHi.lowercase().contains(trimmed) ||
                guide.categoryEn.lowercase().contains(trimmed) ||
                guide.identificationHintHi.lowercase().contains(trimmed) ||
                guide.identificationHintEn.lowercase().contains(trimmed) ||
                guide.basicCareGuideHi.lowercase().contains(trimmed) ||
                guide.basicCareGuideEn.lowercase().contains(trimmed) ||
                guide.diseases.any { d ->
                    d.titleHi.lowercase().contains(trimmed) ||
                        d.titleEn.lowercase().contains(trimmed) ||
                        d.pathogenLabel(AppLanguage.HINDI).lowercase().contains(trimmed) ||
                        d.pathogenLabel(AppLanguage.ENGLISH).lowercase().contains(trimmed) ||
                        d.symptomHi.lowercase().contains(trimmed) ||
                        d.symptomEn.lowercase().contains(trimmed) ||
                        d.detailedSymptomsHi.any { it.lowercase().contains(trimmed) } ||
                        d.detailedSymptomsEn.any { it.lowercase().contains(trimmed) } ||
                        d.tipHi.lowercase().contains(trimmed) ||
                        d.tipEn.lowercase().contains(trimmed)
                }
        }
    }
}

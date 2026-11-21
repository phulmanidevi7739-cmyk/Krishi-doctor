package com.example.data

import com.example.R
import com.example.model.AppLanguage
import com.example.model.ConfidenceLevel
import com.example.model.DemoCase
import com.example.model.HealthStatus
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult

object DemoCasesProvider {

    val sampleCases: List<DemoCase> = listOf(
        DemoCase(
            id = "demo_tomato_blight",
            imageResId = R.drawable.img_demo_tomato_blight,
            cropHi = "टमाटर",
            cropEn = "Tomato",
            titleHi = "टमाटर — संभावित अगेती झुलसा (Early Blight)",
            titleEn = "Tomato — Possible Early Blight",
            summaryHi = "पत्ती पर गोल भूरे धब्बे और पीला घेरा (उदाहरण केस)",
            summaryEn = "Concentric brown rings with yellow halo (Example Case)",
            hindiResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "टमाटर (Tomato)",
                healthStatus = HealthStatus.DISEASED,
                likelyConditionTitle = "संभावित अगेती झुलसा रोग (Early Blight)",
                confidenceLevel = ConfidenceLevel.MODERATE,
                confidenceExplanation = "मध्यम संभावना — यह पत्ती पर दिख रहे गोल छल्लेदार धब्बों के आधार पर AI अनुमान है, पक्की पुष्टि नहीं।",
                visibleSymptoms = listOf(
                    "पत्ती पर गहरे भूरे रंग के गोल धब्बे, जिनमें अंदर गोल छल्ले (चक्र जैसे निशान) दिख रहे हैं।",
                    "धब्बों के चारों ओर हल्का पीलापन फैला हुआ है।",
                    "आमतौर पर यह असर नीचे की पुरानी पत्तियों से शुरू होता है।"
                ),
                possibleCauses = listOf(
                    "यह अल्टरनेरिया (Alternaria) नामक फफूँद के संक्रमण से हो सकता है।",
                    "अधिक नमी, लगातार बारिश या पत्तियों पर लंबे समय तक पानी टिकने से यह रोग तेजी से फैलता है।"
                ),
                treatmentSuggestions = listOf(
                    "शुरुआत में अधिक संक्रमित निचली पत्तियों को तोड़कर खेत से दूर नष्ट करें।",
                    "पौधों के बीच हवा का सही प्रवाह बनाए रखें और ऊपर से सिंचाई (स्प्रिंकलर) से बचें।",
                    "किसी भी फफूँदनाशी (fungicide) का छिड़काव करने से पहले नजदीकी कृषि विज्ञान केंद्र (KVK) या कृषि अधिकारी से सही दवा और मात्रा की पुष्टि करें।"
                ),
                preventionTips = listOf(
                    "रोग-मुक्त प्रमाणित बीज या स्वस्थ पौध का उपयोग करें।",
                    "हर साल एक ही खेत में लगातार टमाटर/आलू न लगाएँ (फसल चक्र अपनाएँ)।",
                    "खेत में जल निकासी की अच्छी व्यवस्था रखें।"
                ),
                whenToConsultExpert = "यदि धब्बे तेजी से ऊपर की नई पत्तियों या तने और फलों पर फैलने लगें, या 3–4 दिनों में सुधार न दिखे, तो तुरंत नजदीकी कृषि विज्ञान केंद्र (KVK) या कृषि सहायक से पत्ती दिखाकर सलाह लें।",
                resultLanguage = AppLanguage.HINDI,
                isDemo = true
            ),
            englishResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "Tomato",
                healthStatus = HealthStatus.DISEASED,
                likelyConditionTitle = "Possible Early Blight (Fungal Infection)",
                confidenceLevel = ConfidenceLevel.MODERATE,
                confidenceExplanation = "Moderate confidence — This is a cautious AI estimate based on visible target-like spots, not a confirmed lab diagnosis.",
                visibleSymptoms = listOf(
                    "Dark brown circular spots with concentric target-like rings on the leaf surface.",
                    "Light yellow halo surrounding the dark lesions.",
                    "Often starts on older lower leaves before moving upward."
                ),
                possibleCauses = listOf(
                    "Likely associated with Alternaria fungal infection.",
                    "Warm, humid weather and prolonged leaf wetness encourage spread."
                ),
                treatmentSuggestions = listOf(
                    "Carefully remove heavily affected lower leaves and dispose of them away from the field.",
                    "Avoid overhead watering in the evening so leaves dry quickly.",
                    "Consult a local Krishi Vigyan Kendra (KVK) or agriculture officer before selecting and spraying any recommended fungicide."
                ),
                preventionTips = listOf(
                    "Use healthy seedlings and disease-tolerant varieties.",
                    "Practice crop rotation instead of planting tomato or potato in the same plot every season.",
                    "Ensure proper spacing and good field drainage."
                ),
                whenToConsultExpert = "Visit your nearest KVK or local agricultural expert immediately if spots spread rapidly to stems, flowers, or fruits.",
                resultLanguage = AppLanguage.ENGLISH,
                isDemo = true
            )
        ),
        DemoCase(
            id = "demo_wheat_rust",
            imageResId = R.drawable.img_demo_wheat_rust,
            cropHi = "गेहूँ",
            cropEn = "Wheat",
            titleHi = "गेहूँ — संभावित पत्ती रतुआ (Leaf Rust)",
            titleEn = "Wheat — Possible Leaf Rust",
            summaryHi = "पत्ती पर नारंगी-भूरे पाउडर जैसे दाने (उदाहरण केस)",
            summaryEn = "Orange-brown powdery pustules on blade (Example Case)",
            hindiResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "गेहूँ (Wheat)",
                healthStatus = HealthStatus.DISEASED,
                likelyConditionTitle = "संभावित पत्ती रतुआ / गेरुई रोग (Leaf Rust)",
                confidenceLevel = ConfidenceLevel.MODERATE,
                confidenceExplanation = "मध्यम संभावना — पत्ती पर नारंगी-भूरे उभरे दानों के आधार पर संभावित संकेत; पक्की पुष्टि विशेषज्ञ जाँच से होगी।",
                visibleSymptoms = listOf(
                    "पत्ती की सतह पर छोटे-छोटे नारंगी-भूरे रंग के पाउडर जैसे उभरे दाने दिख रहे हैं।",
                    "ये दाने पत्ती पर बिखरे हुए हैं और छूने पर हल्का रंग लग सकता है।"
                ),
                possibleCauses = listOf(
                    "यह पक्सिनिया (Puccinia) नामक फफूँद के कारण हो सकता है।",
                    "हवा के साथ बीजाणु एक खेत से दूसरे खेत में फैलते हैं, खासकर हल्की ठंड और नमी में।"
                ),
                treatmentSuggestions = listOf(
                    "रोग दिखने पर खेत के बाकी हिस्सों में भी पत्तियों की जाँच करें।",
                    "कृषि विशेषज्ञ या KVK की सलाह के बिना कोई भी रासायनिक छिड़काव न करें; दवा का नाम, मात्रा और समय विशेषज्ञ व पैकेट के निर्देश के अनुसार ही तय करें।"
                ),
                preventionTips = listOf(
                    "रतुआ-प्रतिरोधी (Rust-resistant) गेहूँ की उन्नत किस्मों की बुवाई करें।",
                    "संतुलित उर्वरक का प्रयोग करें; अधिक यूरिया (नाइट्रोजन) डालने से बचें।"
                ),
                whenToConsultExpert = "गेहूँ में रतुआ रोग तेजी से फैल सकता है। खेत के कई पौधों पर ये दाने दिखते ही नजदीकी कृषि विभाग अधिकारी या KVK केंद्र से संपर्क करें।",
                resultLanguage = AppLanguage.HINDI,
                isDemo = true
            ),
            englishResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "Wheat",
                healthStatus = HealthStatus.DISEASED,
                likelyConditionTitle = "Possible Wheat Leaf Rust",
                confidenceLevel = ConfidenceLevel.MODERATE,
                confidenceExplanation = "Moderate confidence — Based cautiously on orange-brown pustules visible on the leaf blade; confirm locally before spraying.",
                visibleSymptoms = listOf(
                    "Small, scattered orange-brown powdery pustules along the upper leaf surface.",
                    "Mild yellowing around heavily clustered spots."
                ),
                possibleCauses = listOf(
                    "Likely caused by rust fungus (Puccinia species).",
                    "Spores spread easily by wind during cool, moist weather."
                ),
                treatmentSuggestions = listOf(
                    "Inspect nearby wheat plants across the field to see how widespread the symptoms are.",
                    "Speak with a local agricultural officer or KVK before applying any fungicide; strictly follow expert advice and product label dosage."
                ),
                preventionTips = listOf(
                    "Sow rust-resistant wheat varieties recommended for your region.",
                    "Avoid excessive nitrogen (urea) application, which makes plants more susceptible."
                ),
                whenToConsultExpert = "Because wheat rust spreads quickly across fields, consult a local agriculture specialist as soon as multiple plants show pustules.",
                resultLanguage = AppLanguage.ENGLISH,
                isDemo = true
            )
        ),
        DemoCase(
            id = "demo_healthy_rice",
            imageResId = R.drawable.img_demo_healthy_rice,
            cropHi = "धान",
            cropEn = "Rice (Paddy)",
            titleHi = "धान — पत्ती स्वस्थ दिख रही है",
            titleEn = "Rice (Paddy) — Healthy Leaf Appearance",
            summaryHi = "हरा रंग, कोई धब्बा या कीट क्षति नहीं (उदाहरण केस)",
            summaryEn = "Vibrant green blade with no visible spots (Example Case)",
            hindiResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "धान (Rice / Paddy)",
                healthStatus = HealthStatus.HEALTHY,
                likelyConditionTitle = "पत्ती सामान्य और स्वस्थ दिख रही है",
                confidenceLevel = ConfidenceLevel.HIGH,
                confidenceExplanation = "अच्छी संभावना (सतर्क अनुमान) — इस फोटो में रोग के स्पष्ट निशान नहीं दिख रहे, फिर भी पूरे पौधे और जड़ों की जाँच जरूरी है।",
                visibleSymptoms = listOf(
                    "पत्ती का रंग एक समान गहरा और ताज़ा हरा है।",
                    "किसी फफूँद के धब्बे, पीलापन या कीड़ों के काटने के निशान नहीं दिख रहे।"
                ),
                possibleCauses = listOf(
                    "उचित पोषण, पानी और अच्छी धूप के कारण पौधा स्वस्थ दिख रहा है।"
                ),
                treatmentSuggestions = listOf(
                    "किसी दवा या छिड़काव की आवश्यकता नहीं दिख रही है।",
                    "फसल की नियमित देखभाल और संतुलित सिंचाई जारी रखें।"
                ),
                preventionTips = listOf(
                    "सप्ताह में 1–2 बार खेत में घूमकर नीचे की पत्तियों और तने की जाँच करते रहें।",
                    "खेत की मेड़ें साफ रखें और संतुलित खाद का उपयोग करें।"
                ),
                whenToConsultExpert = "यदि पौधे की बढ़वार रुके, जड़ों में सड़न दिखे या अन्य पत्तियाँ पीली पड़ें, तो नजदीकी कृषि विशेषज्ञ से सलाह लें।",
                resultLanguage = AppLanguage.HINDI,
                isDemo = true
            ),
            englishResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.VALID_LEAF,
                qualityMessage = "",
                detectedCrop = "Rice (Paddy)",
                healthStatus = HealthStatus.HEALTHY,
                likelyConditionTitle = "Leaf Appears Healthy",
                confidenceLevel = ConfidenceLevel.HIGH,
                confidenceExplanation = "Higher confidence (cautious estimate) — No clear disease spots are visible in this photo, though inspecting the whole plant is still wise.",
                visibleSymptoms = listOf(
                    "Uniform, vibrant green leaf coloration.",
                    "No dark lesions, yellowing patches, or visible insect chewing marks."
                ),
                possibleCauses = listOf(
                    "Good nutrient balance, water management, and healthy growth conditions."
                ),
                treatmentSuggestions = listOf(
                    "No spray or chemical treatment is needed based on this leaf.",
                    "Continue routine irrigation and balanced crop care."
                ),
                preventionTips = listOf(
                    "Walk the field once or twice a week to check lower stems and newer leaves.",
                    "Keep field bunds clean of weeds to reduce pest shelter."
                ),
                whenToConsultExpert = "Consult an agriculture expert if nearby plants show stunted growth, root rot, or sudden yellowing.",
                resultLanguage = AppLanguage.ENGLISH,
                isDemo = true
            )
        ),
        DemoCase(
            id = "demo_unclear_example",
            imageResId = null,
            cropHi = "पता नहीं",
            cropEn = "Unknown",
            titleHi = "धुंधली या गैर-पत्ती फोटो पर ऐप का व्यवहार",
            titleEn = "How App Handles Unclear / Non-Leaf Photos",
            summaryHi = "गलत अनुमान बनाने के बजाय साफ़ फोटो मांगने का उदाहरण",
            summaryEn = "Example showing polite request for clearer leaf photo",
            hindiResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.UNCLEAR_IMAGE,
                qualityMessage = "यह फोटो धुंधली है या इसमें पौधे की पत्ती साफ दिखाई नहीं दे रही है। गलत सलाह से बचने के लिए कृषि डॉक्टर बिना साफ फोटो के रोग का अनुमान नहीं लगाता। कृपया दिन की रोशनी में पत्ती की नजदीकी और साफ फोटो दोबारा लें।",
                detectedCrop = "अस्पष्ट",
                healthStatus = HealthStatus.UNCERTAIN,
                likelyConditionTitle = "साफ फोटो आवश्यक है",
                confidenceLevel = ConfidenceLevel.LOW,
                confidenceExplanation = "फोटो स्पष्ट न होने के कारण जांच रोक दी गई है।",
                visibleSymptoms = emptyList(),
                possibleCauses = emptyList(),
                treatmentSuggestions = emptyList(),
                preventionTips = emptyList(),
                whenToConsultExpert = "यदि पत्ती साफ दिखाने पर भी समस्या समझ न आए, तो नजदीकी कृषि विज्ञान केंद्र (KVK) जाएं।",
                resultLanguage = AppLanguage.HINDI,
                isDemo = true
            ),
            englishResult = LeafDiagnosisResult(
                imageQualityStatus = ImageQualityStatus.UNCLEAR_IMAGE,
                qualityMessage = "This photo is too blurry, dark, or does not clearly show a plant leaf. Instead of guessing a diagnosis, Krishi Doctor asks you to take a clear close-up photo of one leaf in bright daylight and upload again.",
                detectedCrop = "Unclear",
                healthStatus = HealthStatus.UNCERTAIN,
                likelyConditionTitle = "Clearer Photo Needed",
                confidenceLevel = ConfidenceLevel.LOW,
                confidenceExplanation = "Analysis paused because the leaf details could not be seen clearly.",
                visibleSymptoms = emptyList(),
                possibleCauses = emptyList(),
                treatmentSuggestions = emptyList(),
                preventionTips = emptyList(),
                whenToConsultExpert = "Visit your nearest Krishi Vigyan Kendra (KVK) if you need in-person plant inspection.",
                resultLanguage = AppLanguage.ENGLISH,
                isDemo = true
            )
        )
    )
}

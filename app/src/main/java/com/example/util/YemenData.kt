package com.example.util

import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert

object YemenData {
    val governorates = listOf(
        "كافة المحافظات",
        "أمانة العاصمة",
        "صنعاء",
        "عدن",
        "تعز",
        "إب",
        "الحديدة",
        "حضرموت",
        "مأرب",
        "ذمار",
        "شبوة",
        "لحج",
        "أبين",
        "حجة",
        "صعدة",
        "المحويت",
        "عمران",
        "الضالع",
        "البيضاء",
        "الجوف",
        "المهرة",
        "سقطرى",
        "ريمة"
    )

    val governoratesOnly = governorates.filter { it != "كافة المحافظات" }

    val phoneBrands = listOf(
        "Samsung",
        "Apple iPhone",
        "Xiaomi / Redmi",
        "Huawei",
        "Honor",
        "Infinix",
        "Tecno",
        "Realme",
        "OnePlus",
        "Google Pixel",
        "Oppo",
        "Vivo",
        "أخرى"
    )

    val reportStatuses = listOf(
        "الكل",
        "مسروق",
        "مفقود",
        "تم الاسترجاع",
        "قيد التحري"
    )

    fun getInitialReports(): List<PhoneReport> {
        val now = System.currentTimeMillis()
        return listOf(
            PhoneReport(
                id = 1,
                brand = "Samsung",
                modelName = "Galaxy S23 Ultra",
                imei1 = "356789123456789",
                imei2 = "356789123456790",
                serialNumber = "R58M123456",
                color = "أسود شبحي",
                storageCapacity = "256 GB",
                governorate = "أمانة العاصمة",
                district = "شارع حدة - أمام مجمع الكميم",
                incidentDate = "2025-02-28",
                description = "تمت سرقة الهاتف أثناء النزول من الباص، الجهاز بحالة ممتازة وبداخله شريحتي يمن موبايل وسبأفون وذاكرة هامة جداً.",
                distinctiveFeatures = "خدش طفيف بجانب منفذ الشاحن مع كفر حماية سيليكون أزرق",
                ownerName = "أحمد محمد الحاشدي",
                contactPhone = "771234567",
                whatsappNumber = "771234567",
                policeStation = "قسم شرطة حدة - بلاغ رقم 4082",
                rewardAmount = 100000L,
                status = "مسروق",
                createdAt = now - (1000 * 60 * 45), // 45 mins ago
                isUrgent = true
            ),
            PhoneReport(
                id = 2,
                brand = "Apple iPhone",
                modelName = "iPhone 14 Pro Max",
                imei1 = "354123098765432",
                imei2 = "",
                serialNumber = "F2LMN8940PQR",
                color = "بنفسجي غامق",
                storageCapacity = "512 GB",
                governorate = "عدن",
                district = "مديرية المنصورة - شارع التسعين",
                incidentDate = "2025-02-27",
                description = "تم خطف الهاتف من قبل دراجة نارية أثناء المشي بجانب الرصيف، الجهاز موضوع عليه وضع الفقدان (Lost Mode).",
                distinctiveFeatures = "ستيكر حماية زجاجي أمامي لاصق عليه حماية كاميرا معدنية",
                ownerName = "سالم عمر باعباد",
                contactPhone = "733456789",
                whatsappNumber = "733456789",
                policeStation = "شرطة المنصورة - عدن",
                rewardAmount = 250000L,
                status = "مسروق",
                createdAt = now - (1000 * 60 * 180), // 3 hours ago
                isUrgent = true
            ),
            PhoneReport(
                id = 3,
                brand = "Xiaomi / Redmi",
                modelName = "Redmi Note 13 Pro+",
                imei1 = "869450123789456",
                imei2 = "869450123789457",
                serialNumber = "28394019283",
                color = "أبيض قمرى",
                storageCapacity = "256 GB",
                governorate = "تعز",
                district = "شارع جمال عبد الناصر - بالقرب من جولة العواضي",
                incidentDate = "2025-02-26",
                description = "نسيان الهاتف في أحد المحلات التجارية وتم أخذه من شخص مجهول.",
                distinctiveFeatures = "لاصق شعار نادي على ظهر الهاتف",
                ownerName = "مهند علي الصبري",
                contactPhone = "715678912",
                whatsappNumber = "715678912",
                policeStation = "مركز شرطة الباب الكبير",
                rewardAmount = 50000L,
                status = "مسروق",
                createdAt = now - (1000 * 60 * 60 * 24), // 1 day ago
                isUrgent = false
            ),
            PhoneReport(
                id = 4,
                brand = "Infinix",
                modelName = "Zero 30 5G",
                imei1 = "359876543210987",
                imei2 = "359876543210988",
                serialNumber = "INF892301",
                color = "ذهبي متدرج",
                storageCapacity = "256 GB",
                governorate = "إب",
                district = "الدائري الغربي - بالقرب من جامعة إب",
                incidentDate = "2025-02-25",
                description = "فقدان الجهاز في سيارة أجرة متجهة من الدائري إلى المعاين.",
                distinctiveFeatures = "كفر شفاف مع تعليقة يد بنية",
                ownerName = "خالد قاسم العواضي",
                contactPhone = "770987654",
                whatsappNumber = "770987654",
                policeStation = "إدارة أمن محافظة إب",
                rewardAmount = 40000L,
                status = "مفقود",
                createdAt = now - (1000 * 60 * 60 * 36),
                isUrgent = false
            ),
            PhoneReport(
                id = 5,
                brand = "Samsung",
                modelName = "Galaxy A54 5G",
                imei1 = "358901234567890",
                imei2 = "358901234567891",
                serialNumber = "SM-A546B-99",
                color = "ليموني أخضر",
                storageCapacity = "128 GB",
                governorate = "حضرموت",
                district = "المكلا - خور المكلا",
                incidentDate = "2025-02-20",
                description = "تم العثور على الجهاز وضبطه بفضل فحص رقم الـ IMEI في أحد محلات الجوالات وإعادته لمالكه الشرعي.",
                distinctiveFeatures = "شاشة حماية مكسورة من الزاوية اليمنى السفلى",
                ownerName = "فؤاد حسن باوزير",
                contactPhone = "777890123",
                whatsappNumber = "777890123",
                policeStation = "بحث جنائي المكلا",
                rewardAmount = 60000L,
                status = "تم الاسترجاع",
                createdAt = now - (1000 * 60 * 60 * 72),
                isUrgent = false
            ),
            PhoneReport(
                id = 6,
                brand = "Tecno",
                modelName = "Camon 20 Pro",
                imei1 = "864501928374650",
                imei2 = "864501928374651",
                serialNumber = "CK8N-2023",
                color = "أزرق داكن",
                storageCapacity = "256 GB",
                governorate = "مأرب",
                district = "شارع الروضة - المدينة",
                incidentDate = "2025-02-28",
                description = "سرقة الجوال أثناء صلاة الجمعة في الجامع الكبير.",
                distinctiveFeatures = "كفر أسود مصفح ضد الصدمات",
                ownerName = "ياسر صالح المرادي",
                contactPhone = "773124578",
                whatsappNumber = "773124578",
                policeStation = "قسم شرطة المدينة مأرب",
                rewardAmount = 80000L,
                status = "مسروق",
                createdAt = now - (1000 * 60 * 120),
                isUrgent = true
            )
        )
    }

    fun getInitialAlerts(): List<UrgentAlert> {
        val now = System.currentTimeMillis()
        return listOf(
            UrgentAlert(
                id = 1,
                reportId = 1,
                title = "🚨 تعميم سرقة عاجل - أمانة العاصمة",
                message = "تم الإبلاغ عن سرقة Samsung Galaxy S23 Ultra (أسود) في شارع حدة. يرجى من جميع محلات الهواتف فحص الـ IMEI قبل الشراء.",
                governorate = "أمانة العاصمة",
                phoneModel = "Samsung Galaxy S23 Ultra",
                imeiSnippet = "356789...789",
                timestamp = now - (1000 * 60 * 45),
                isRead = false,
                severity = "CRITICAL"
            ),
            UrgentAlert(
                id = 2,
                reportId = 2,
                title = "🚨 تعميم سرقة عاجل - عدن",
                message = "تم الإبلاغ عن نهب iPhone 14 Pro Max (بنفسجي) في المنصورة. مكافأة مجزية 250,000 ريال يمني لمن يدلي بمعلومات.",
                governorate = "عدن",
                phoneModel = "iPhone 14 Pro Max",
                imeiSnippet = "354123...432",
                timestamp = now - (1000 * 60 * 180),
                isRead = false,
                severity = "CRITICAL"
            ),
            UrgentAlert(
                id = 3,
                reportId = 6,
                title = "⚠️ بلاغ سرقة جديد - مأرب",
                message = "تم سرقة Tecno Camon 20 Pro في شارع الروضة بمدينة مأرب. السيريال معمم أمنياً.",
                governorate = "مأرب",
                phoneModel = "Tecno Camon 20 Pro",
                imeiSnippet = "864501...650",
                timestamp = now - (1000 * 60 * 120),
                isRead = false,
                severity = "WARNING"
            ),
            UrgentAlert(
                id = 4,
                reportId = 5,
                title = "✅ بشرى: تم استرجاع هاتف بنجاح في حضرموت",
                message = "تم ضبط هاتف Samsung A54 وتسليمه لصاحبه في المكلا بعد مطابقة الـ IMEI في التطبيق.",
                governorate = "حضرموت",
                phoneModel = "Samsung Galaxy A54",
                imeiSnippet = "358901...890",
                timestamp = now - (1000 * 60 * 60 * 72),
                isRead = true,
                severity = "RESOLVED"
            )
        )
    }
}

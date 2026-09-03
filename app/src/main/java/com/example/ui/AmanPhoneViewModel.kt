package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ImeiCheckRecord
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.data.repository.PhoneReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ImeiCheckState {
    object Idle : ImeiCheckState
    object Loading : ImeiCheckState
    data class Clean(val imei: String) : ImeiCheckState
    data class StolenFound(val report: PhoneReport) : ImeiCheckState
    data class Error(val message: String) : ImeiCheckState
}

class AmanPhoneViewModel(
    private val repository: PhoneReportRepository
) : ViewModel() {

    // Current User Session
    private val _currentUser = MutableStateFlow<com.example.data.model.AppUser?>(null)
    val currentUser: StateFlow<com.example.data.model.AppUser?> = _currentUser.asStateFlow()

    private val _isUserLoading = MutableStateFlow(true)
    val isUserLoading: StateFlow<Boolean> = _isUserLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow<String?>(null)
    val lastSyncMessage: StateFlow<String?> = _lastSyncMessage.asStateFlow()

    init {
        // Load last logged-in user if available
        viewModelScope.launch {
            try {
                val lastUser = repository.getLastActiveUser()
                _currentUser.value = lastUser
            } catch (_: Exception) {
            } finally {
                _isUserLoading.value = false
            }
        }
        syncNow()
    }

    fun syncNow(onFinished: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val newCount = repository.syncNow(showNotificationForNewAlerts = true)
                _lastSyncMessage.value = if (newCount > 0) {
                    "تم استلام $newCount بلاغ وتعميم جديد بنجاح 🚨"
                } else {
                    "متصل بالبث السحابي المباشر - كافة البلاغات محدثة ✅"
                }
                onFinished?.invoke(newCount)
            } catch (_: Exception) {
                _lastSyncMessage.value = "تعذر الاتصال بالبث السحابي، تحقق من الإنترنت"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun onUserLogin(user: com.example.data.model.AppUser) {
        viewModelScope.launch {
            repository.saveUser(user.copy(lastLoginAt = System.currentTimeMillis()))
            _currentUser.value = user
        }
    }

    fun onUserLogout() {
        val user = _currentUser.value
        _currentUser.value = null
        if (user != null) {
            viewModelScope.launch {
                repository.saveUser(user.copy(lastLoginAt = 0L))
            }
        }
    }

    suspend fun findUserByEmail(email: String): com.example.data.model.AppUser? {
        return repository.getUserByEmail(email)
    }

    // Filters
    val selectedGovernorate = MutableStateFlow("كافة المحافظات")
    val selectedStatus = MutableStateFlow("الكل")
    val searchQuery = MutableStateFlow("")

    // Reports Feed
    val filteredReports: StateFlow<List<PhoneReport>> = combine(
        repository.allReports,
        selectedGovernorate,
        selectedStatus,
        searchQuery
    ) { reports, gov, status, query ->
        reports.filter { report ->
            val matchGov = gov == "كافة المحافظات" || report.governorate == gov
            val matchStatus = status == "الكل" || report.status == status
            val matchQuery = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                report.brand.lowercase().contains(q) ||
                        report.modelName.lowercase().contains(q) ||
                        report.imei1.contains(q) ||
                        report.imei2.contains(q) ||
                        report.governorate.lowercase().contains(q) ||
                        report.district.lowercase().contains(q) ||
                        report.description.lowercase().contains(q) ||
                        report.ownerName.lowercase().contains(q)
            }
            matchGov && matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Urgent Alerts & Stats
    val allAlerts: StateFlow<List<UrgentAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertsCount: StateFlow<Int> = repository.unreadAlertsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalReportsCount: StateFlow<Int> = repository.totalReportsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recoveredReportsCount: StateFlow<Int> = repository.recoveredReportsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentChecks: StateFlow<List<ImeiCheckRecord>> = repository.recentImeiChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Report for Details Sheet
    private val _selectedReport = MutableStateFlow<PhoneReport?>(null)
    val selectedReport: StateFlow<PhoneReport?> = _selectedReport.asStateFlow()

    // IMEI Checker State
    private val _imeiInput = MutableStateFlow("")
    val imeiInput: StateFlow<String> = _imeiInput.asStateFlow()

    private val _imeiCheckState = MutableStateFlow<ImeiCheckState>(ImeiCheckState.Idle)
    val imeiCheckState: StateFlow<ImeiCheckState> = _imeiCheckState.asStateFlow()

    // New Report Form States
    val formBrand = MutableStateFlow("Samsung")
    val formModel = MutableStateFlow("")
    val formImei1 = MutableStateFlow("")
    val formImei2 = MutableStateFlow("")
    val formSerialNumber = MutableStateFlow("")
    val formColor = MutableStateFlow("")
    val formStorage = MutableStateFlow("128 GB")
    val formGovernorate = MutableStateFlow("أمانة العاصمة")
    val formDistrict = MutableStateFlow("")
    val formIncidentDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val formDescription = MutableStateFlow("")
    val formDistinctiveFeatures = MutableStateFlow("")
    val formOwnerName = MutableStateFlow("")
    val formContactPhone = MutableStateFlow("")
    val formWhatsapp = MutableStateFlow("")
    val formPoliceStation = MutableStateFlow("")
    val formRewardAmount = MutableStateFlow("")
    val formStatus = MutableStateFlow("مسروق")

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionSuccessMessage = MutableStateFlow<String?>(null)
    val submissionSuccessMessage: StateFlow<String?> = _submissionSuccessMessage.asStateFlow()

    private val _formErrorMessage = MutableStateFlow<String?>(null)
    val formErrorMessage: StateFlow<String?> = _formErrorMessage.asStateFlow()

    fun onGovernorateSelected(gov: String) {
        selectedGovernorate.value = gov
    }

    fun onStatusSelected(status: String) {
        selectedStatus.value = status
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun selectReport(report: PhoneReport?) {
        _selectedReport.value = report
    }

    fun onImeiInputChanged(input: String) {
        _imeiInput.value = input.filter { it.isDigit() }.take(15)
        if (_imeiCheckState.value !is ImeiCheckState.Idle) {
            _imeiCheckState.value = ImeiCheckState.Idle
        }
    }

    fun setQuickCheckImei(imei: String) {
        _imeiInput.value = imei
        performImeiCheck()
    }

    fun performImeiCheck() {
        val input = _imeiInput.value.trim()
        if (input.length < 14) {
            _imeiCheckState.value = ImeiCheckState.Error("يرجى إدخال رقم IMEI صحيح يتكون من 15 رقماً (أو 14 على الأقل). يمكنك معرفته عبر طلب الكود *#06#")
            return
        }

        viewModelScope.launch {
            _imeiCheckState.value = ImeiCheckState.Loading
            val (foundReport, isStolen) = repository.checkImei(input)
            if (isStolen && foundReport != null) {
                _imeiCheckState.value = ImeiCheckState.StolenFound(foundReport)
            } else if (foundReport != null && foundReport.status == "تم الاسترجاع") {
                _imeiCheckState.value = ImeiCheckState.Clean(input)
            } else {
                _imeiCheckState.value = ImeiCheckState.Clean(input)
            }
        }
    }

    fun resetImeiCheck() {
        _imeiInput.value = ""
        _imeiCheckState.value = ImeiCheckState.Idle
    }

    fun submitReport(onSuccess: () -> Unit) {
        val brand = formBrand.value.trim()
        val model = formModel.value.trim()
        val imei1 = formImei1.value.trim().filter { it.isDigit() }
        val gov = formGovernorate.value
        val district = formDistrict.value.trim()
        val owner = formOwnerName.value.trim()
        val phone = formContactPhone.value.trim()

        if (model.isBlank()) {
            _formErrorMessage.value = "يرجى كتابة موديل الهاتف (مثال: Galaxy S23 أو iPhone 14)"
            return
        }
        if (imei1.length < 14) {
            _formErrorMessage.value = "رقم الـ IMEI غير صالح! يجب أن يتكون من 15 رقماً"
            return
        }
        if (district.isBlank()) {
            _formErrorMessage.value = "يرجى تحديد مكان السرقة (المديرية أو الشارع)"
            return
        }
        if (owner.isBlank()) {
            _formErrorMessage.value = "يرجى كتابة اسم صاحب البلاغ"
            return
        }
        if (phone.length < 9) {
            _formErrorMessage.value = "يرجى كتابة رقم هاتف تواصل يمني صحيح (مثال: 771234567)"
            return
        }

        _formErrorMessage.value = null
        _isSubmitting.value = true

        viewModelScope.launch {
            val reward = formRewardAmount.value.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val report = PhoneReport(
                brand = brand,
                modelName = model,
                imei1 = imei1,
                imei2 = formImei2.value.filter { it.isDigit() },
                serialNumber = formSerialNumber.value.trim(),
                color = formColor.value.trim().ifEmpty { "غير محدد" },
                storageCapacity = formStorage.value,
                governorate = gov,
                district = district,
                incidentDate = formIncidentDate.value,
                description = formDescription.value.trim().ifEmpty { "تم الإبلاغ عن سرقة الهاتف ومطلوب تعميم البلاغ أمنياً" },
                distinctiveFeatures = formDistinctiveFeatures.value.trim(),
                ownerName = owner,
                contactPhone = phone,
                whatsappNumber = formWhatsapp.value.trim().ifEmpty { phone },
                policeStation = formPoliceStation.value.trim(),
                rewardAmount = reward,
                status = formStatus.value,
                createdAt = System.currentTimeMillis(),
                isUrgent = true
            )

            repository.submitReport(report)
            _isSubmitting.value = false
            _submissionSuccessMessage.value = "تم تسجيل البلاغ وبث التنبيه الفوري بنجاح لكافة محافظات الجمهورية اليمنية!"

            // Clear form
            formModel.value = ""
            formImei1.value = ""
            formImei2.value = ""
            formSerialNumber.value = ""
            formColor.value = ""
            formDistrict.value = ""
            formDescription.value = ""
            formDistinctiveFeatures.value = ""
            formRewardAmount.value = ""

            onSuccess()
        }
    }

    fun clearSuccessMessage() {
        _submissionSuccessMessage.value = null
    }

    fun clearErrorMessage() {
        _formErrorMessage.value = null
    }

    fun markReportAsRecovered(report: PhoneReport) {
        viewModelScope.launch {
            repository.updateReportStatus(
                reportId = report.id,
                newStatus = "تم الاسترجاع",
                phoneModel = "${report.brand} ${report.modelName}",
                gov = report.governorate
            )
            _selectedReport.value = report.copy(status = "تم الاسترجاع")
        }
    }

    fun markAlertAsRead(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    fun markAllAlertsAsRead() {
        viewModelScope.launch {
            repository.markAllAlertsAsRead()
        }
    }
}

class AmanPhoneViewModelFactory(
    private val repository: PhoneReportRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AmanPhoneViewModel::class.java)) {
            return AmanPhoneViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

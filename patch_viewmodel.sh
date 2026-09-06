sed -i 's/val report = PhoneReport(/var uploadedImageUrl = ""\n            val uri = formProofImageUri.value\n            if (uri != null) {\n                val url = repository.cloudSyncManager.uploadProofImage(uri)\n                if (url != null) uploadedImageUrl = url\n            }\n\n            val report = PhoneReport(/g' app/src/main/java/com/example/ui/AmanPhoneViewModel.kt

sed -i 's/isUrgent = true/isUrgent = true,\n                proofImageUrl = uploadedImageUrl/g' app/src/main/java/com/example/ui/AmanPhoneViewModel.kt

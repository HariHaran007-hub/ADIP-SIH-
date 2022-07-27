package com.rcappstudio.adip.ui

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.AidsDoc
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.databinding.FragmentApplicationStatusBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog
import com.squareup.picasso.Picasso
import java.util.*

class ApplicationStatusFragment : Fragment() {

    private lateinit var binding: FragmentApplicationStatusBinding

    private lateinit var databaseReference: DatabaseReference

    private lateinit var userPath: String
    private lateinit var pref: SharedPreferences

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApplicationStatusBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading application status....")
        loadingDialog.startLoading()
        initDataModule()
    }

    private fun initDataModule() {
        pref = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        userPath = pref.getString(Constants.USER_PROFILE_PATH, null)!!
        databaseReference = FirebaseDatabase.getInstance().getReference(userPath)
        loadDatabase()
    }

    private fun loadDatabase() {
        databaseReference.child("/${Constants.REQUEST_STATUS}")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val requestStatus = snapshot.getValue(RequestStatus::class.java)
                    Log.d("aidsList", "loadDatabase: ${requestStatus!!.aidsList!!.size}")
                    loadUserDetailsFromDatabase(requestStatus)
                } else {
                    loadingDialog.isDismiss()
                }
            }
            .addOnFailureListener {
                //TODO: Error occurs show pop up dialog

            }
    }

    private fun loadUserDetailsFromDatabase(requestStatus: RequestStatus?) {
        if (requestStatus != null) {
            databaseReference.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        loadImageUrl(requestStatus, user)
                    } else {
                        loadingDialog.isDismiss()
                    }
                }
        }
    }

    private fun loadImageUrl(requestStatus: RequestStatus?, user: UserModel?) {
        databaseReference.child("/${Constants.AIDS_VERIFICATION_DOC}")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val aidsDoc = snapshot.getValue(AidsDoc::class.java)
                    initView(requestStatus, user, aidsDoc)
                }
            }
    }

    private fun initView(requestStatus: RequestStatus?, user: UserModel?, aidsDoc: AidsDoc?) {


        if (requestStatus != null && user != null && aidsDoc != null) {
            if (user.alreadyApplied) {
                //TODO: Date of applied yet to be added
                binding.applicationStatusCardView.visibility = View.VISIBLE
                binding.lottieFile.visibility = View.GONE
                binding.tvNoStatus.visibility = View.GONE

                binding.tvName.text = "Name: ".plus(user.name)
                binding.tvMobileNo.text = "Mobile no: ".plus(user.mobileNo)
               // Log.d("dateView", "initView: ${getDateTime(requestStatus.appliedOnTimeStamp!!)}")
               binding.tvAppliedOn.text = "Applied on: ".plus(getDateTime(requestStatus.appliedOnTimeStamp!!))

                Picasso.get()
                    .load(user.profileImageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.no_profile)
                    .error(R.drawable.no_profile)
                    .into(binding.profileImage)



                if (requestStatus.verified) {
                    binding.verificationStatusCardView.strokeColor =
                        requireContext().resources.getColor(R.color.green)
                    binding.verificationStatusCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.greenLight
                        )
                    )
                    binding.statusTextView.text = "Application verified and approved"
                    binding.aidsDeliveryCardView.visibility = View.VISIBLE
                    binding.mapButton.visibility = View.VISIBLE

                    if (requestStatus.aidsReceived) {
                        //TODO: Update UI accordingly and bind location to google map button
                        binding.aidsDeliveryTextView.text = "Aids / appliance received"
                        binding.aidsDeliveryCardView.strokeColor =
                            requireContext().resources.getColor(R.color.green)
                        binding.aidsDeliveryCardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.greenLight
                            )
                        )
                    }
                }

                if (requestStatus.notAppropriate) {
                    binding.verificationStatusCardView.strokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red)
                    binding.verificationStatusCardView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.redLight
                        )
                    )
                    binding.statusTextView.text =
                        "Application rejected please visit registration portal to re upload the files"
                }
                loadingDialog.isDismiss()

                clickListener(aidsDoc)

            } else {
                //Not applied
                loadingDialog.isDismiss()
            }
            loadingDialog.isDismiss()
        }
        loadingDialog.isDismiss()
    }

    private fun clickListener(aidsDoc: AidsDoc) {

        binding.tvDisabilityCertificate.setOnClickListener {
            imagePreviewDialog(aidsDoc.disabilityCertificateURL!!)
        }

        binding.tvAddressProof.setOnClickListener {
            imagePreviewDialog(aidsDoc.addressProofUrl!!)
        }

        binding.tvIncomeCertificate.setOnClickListener {
            imagePreviewDialog(aidsDoc.incomeTaxCertificateUrl!!)
        }

        binding.tvPassportSizePhoto.setOnClickListener {
            imagePreviewDialog(aidsDoc.passportSizePhotoURL!!)
        }

        binding.tvIdentityProof.setOnClickListener {
            imagePreviewDialog(aidsDoc.identityProofUrl!!)
        }

        binding.mapButton.setOnClickListener {
            //TODO : Currently google map will be static and it will be added dynamically
            openGoogleMaps()
        }

    }

    private fun openGoogleMaps() {
        val uri =
            "http://maps.google.com/maps?daddr=10.365581,77.970657" + "(" + "Nearest implementation agency" + ")"
        val gmmIntentUri = Uri.parse(uri)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }


    private fun imagePreviewDialog(imageUrl: String) {

        val isdialog: AlertDialog

        val dialogView = layoutInflater.inflate(R.layout.imagepreview_dialog, null)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
        val dialogCancel = dialogView.findViewById<TextView>(R.id.cancelTextView)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        Picasso.get()
            .load(imageUrl)
            .error(R.drawable.no_profile)
            .placeholder(R.drawable.no_profile)
            .into(dialogImageView)
        builder.setCancelable(true)
        isdialog = builder.create()
        isdialog.show()

        dialogCancel.setOnClickListener {
            isdialog.dismiss()
        }

    }

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

}
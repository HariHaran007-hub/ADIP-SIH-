package com.rcappstudio.adip.ui.registrationportal

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.databinding.ConfirmationDialogBinding
import com.rcappstudio.adip.databinding.FragmentRegistrationCategoryBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog

class RegistrationCategoryFragment : Fragment() {

    private lateinit var fBinding : ConfirmationDialogBinding

    private lateinit var binding : FragmentRegistrationCategoryBinding

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegistrationCategoryBinding.inflate(layoutInflater)
        binding.customToolBar.toolbar.title = "Registration portal"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(),"Loading portal info....")
        loadingDialog.startLoading()
        loadDatabase()
        clickListener()
    }



    private fun clickListener(){
        binding.btnContinue.setOnClickListener {
            confirmDialog()
        }
    }

    private fun confirmDialog() {
        fBinding = ConfirmationDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(fBinding.root)
        fBinding.btnRegisterContinue.setOnClickListener{
            Log.d("TAGG", "confirmDialog: ")
            startActivity(Intent(requireContext(), UploadAidsRegistrationDetailsActivity::class.java))
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun loadDatabase(){
        val pref = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE,
            AppCompatActivity.MODE_PRIVATE
        )
        val userPath = pref.getString(Constants.USER_PROFILE_PATH, null)

        FirebaseDatabase.getInstance().getReference("$userPath/alreadyApplied")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(it: DataSnapshot) {
                    if(it.exists()){
                        if(it.value as Boolean){
                            checkIsAppropriateOrNotRequestStatus(userPath!!)

                        } else{
                            binding.btnContinue.visibility = View.VISIBLE
                            binding.llLayout.visibility = View.VISIBLE
                            binding.lottieFile.visibility = View.GONE
                            binding.tvStatus.visibility = View.GONE
                            loadingDialog.isDismiss()
                        }
                        loadingDialog.isDismiss()
                    }
                    loadingDialog.isDismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun checkIsAppropriateOrNotRequestStatus(userPath : String){
        FirebaseDatabase.getInstance().getReference("${userPath}/${Constants.REQUEST_STATUS}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val requestStatus = snapshot.getValue(RequestStatus::class.java)
                        if(requestStatus!!.notAppropriate){
                            binding.btnContinue.visibility = View.VISIBLE
                            binding.llLayout.visibility = View.VISIBLE
                            binding.lottieFile.visibility = View.GONE
                            binding.tvStatus.visibility = View.GONE
                            loadingDialog.isDismiss()
                        } else{
                            binding.btnContinue.visibility = View.GONE
                            binding.llLayout.visibility = View.GONE
                            binding.lottieFile.visibility = View.VISIBLE
                            binding.tvStatus.visibility = View.VISIBLE
                            loadingDialog.isDismiss()
                        }
                    } else{
                        binding.btnContinue.visibility = View.VISIBLE
                        binding.llLayout.visibility = View.VISIBLE
                        binding.lottieFile.visibility = View.GONE
                        binding.tvStatus.visibility = View.GONE
                        loadingDialog.isDismiss()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


}
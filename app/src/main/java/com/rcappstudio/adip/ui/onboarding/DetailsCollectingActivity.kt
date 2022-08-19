package com.rcappstudio.adip.ui.onboarding

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.icu.text.DateFormat.DAY
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.adip.OcrActivity
import com.rcappstudio.adip.R
import com.rcappstudio.adip.databinding.ActivityDetailsCollectingBinding
import com.rcappstudio.adip.utils.Constants
import java.util.*


class DetailsCollectingActivity : AppCompatActivity() {

    private lateinit var selectedState  : String

    private lateinit var selectedDistrict  : String

    private lateinit var stateAdapter : ArrayAdapter<CharSequence>
    private lateinit var districtAdapter : ArrayAdapter<CharSequence>

    private lateinit var  datePickerDialog : DatePickerDialog

    private lateinit var mobileNo : String
    private lateinit var udidNumber : String


    private lateinit var binding : ActivityDetailsCollectingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsCollectingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        mobileNo = intent.getStringExtra("mobile").toString()
        udidNumber = intent.getStringExtra("udidNumber").toString()
        setSpinnerLayout()
        clickListener()
        initDatePicker()
    }


    private fun setSpinnerLayout(){
        stateAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.array_indian_states, R.layout.spinner_layout
        );

        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.stateSpinner.adapter = stateAdapter;

        binding.stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                selectedState = binding.stateSpinner.selectedItem.toString()

                val parentId : Int = adapterView!!.id

                if (parentId == R.id.stateSpinner) {
                    when (selectedState) {
                        "Select Your State" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_default_districts, R.layout.spinner_layout
                        )
                        "Andhra Pradesh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_andhra_pradesh_districts, R.layout.spinner_layout
                        )
                        "Arunachal Pradesh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_arunachal_pradesh_districts, R.layout.spinner_layout
                        )
                        "Assam" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_assam_districts, R.layout.spinner_layout
                        )
                        "Bihar" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_bihar_districts, R.layout.spinner_layout
                        )
                        "Chhattisgarh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_chhattisgarh_districts, R.layout.spinner_layout
                        )
                        "Goa" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_goa_districts, R.layout.spinner_layout
                        )
                        "Gujarat" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_gujarat_districts, R.layout.spinner_layout
                        )
                        "Haryana" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_haryana_districts, R.layout.spinner_layout
                        )
                        "Himachal Pradesh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_himachal_pradesh_districts, R.layout.spinner_layout
                        )
                        "Jharkhand" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_jharkhand_districts, R.layout.spinner_layout
                        )
                        "Karnataka" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_karnataka_districts, R.layout.spinner_layout
                        )
                        "Kerala" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_kerala_districts, R.layout.spinner_layout
                        )
                        "Madhya Pradesh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_madhya_pradesh_districts, R.layout.spinner_layout
                        )
                        "Maharashtra" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_maharashtra_districts, R.layout.spinner_layout
                        )
                        "Manipur" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_manipur_districts, R.layout.spinner_layout
                        )
                        "Meghalaya" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_meghalaya_districts, R.layout.spinner_layout
                        )
                        "Mizoram" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_mizoram_districts, R.layout.spinner_layout
                        )
                        "Nagaland" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_nagaland_districts, R.layout.spinner_layout
                        )
                        "Odisha" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_odisha_districts, R.layout.spinner_layout
                        )
                        "Punjab" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_punjab_districts, R.layout.spinner_layout
                        )
                        "Rajasthan" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_rajasthan_districts, R.layout.spinner_layout
                        )
                        "Sikkim" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_sikkim_districts, R.layout.spinner_layout
                        )
                        "Tamil Nadu" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_tamil_nadu_districts, R.layout.spinner_layout
                        )
                        "Telangana" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_telangana_districts, R.layout.spinner_layout
                        )
                        "Tripura" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_tripura_districts, R.layout.spinner_layout
                        )
                        "Uttar Pradesh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_uttar_pradesh_districts, R.layout.spinner_layout
                        )
                        "Uttarakhand" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_uttarakhand_districts, R.layout.spinner_layout
                        )
                        "West Bengal" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_west_bengal_districts, R.layout.spinner_layout
                        )
                        "Andaman and Nicobar Islands" -> districtAdapter =
                            ArrayAdapter.createFromResource(
                                applicationContext,
                                R.array.array_andaman_nicobar_districts, R.layout.spinner_layout
                            )
                        "Chandigarh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_chandigarh_districts, R.layout.spinner_layout
                        )
                        "Dadra and Nagar Haveli" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_dadra_nagar_haveli_districts, R.layout.spinner_layout
                        )
                        "Daman and Diu" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_daman_diu_districts, R.layout.spinner_layout
                        )
                        "Delhi" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_delhi_districts, R.layout.spinner_layout
                        )
                        "Jammu and Kashmir" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_jammu_kashmir_districts, R.layout.spinner_layout
                        )
                        "Lakshadweep" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_lakshadweep_districts, R.layout.spinner_layout
                        )
                        "Ladakh" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_ladakh_districts, R.layout.spinner_layout
                        )
                        "Puducherry" -> districtAdapter = ArrayAdapter.createFromResource(
                            applicationContext,
                            R.array.array_puducherry_districts, R.layout.spinner_layout
                        )
                        else -> {}
                    }
                    districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Specify the layout to use when the list of choices appears
                    binding.districtSpinner.adapter =  districtAdapter //Populate the list of Districts in respect of the State selected

                    //To obtain the selected District from the spinner
                    binding.districtSpinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            selectedDistrict = binding.districtSpinner.selectedItem.toString()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun clickListener(){
        binding.notHaveUdid.setOnClickListener {
//            startActivity(Intent(applicationContext , WebViewActivity::class.java))
            openBrowser()
        }
        binding.datePicker.setOnClickListener{
            openDatePicker(binding.root)
        }

        binding.btnContine.setOnClickListener {
            validateForm()
        }
    }

    private fun validateForm(){
        //TODO: Validation need to be checked
        if(binding.etName.text.isNullOrEmpty()){
           binding.etName.requestFocus()
           binding.etName.error = "Name required"
            return
        }

        if(binding.datePicker.text.isNullOrEmpty()){
            binding.datePicker.requestFocus()
            binding.datePicker.error = "Date Required"
            return
        }

        if(binding.stateSpinner.selectedItem.toString() == "State"){
            binding.tvStateSpinner.requestFocus()
            binding.tvStateSpinner.error = "State required"
            return
        }

        checkDataValidity()

    }

    private fun checkDataValidity(){
        val intent = Intent(applicationContext, OcrActivity::class.java)
        intent.putExtra(NAME, binding.etName.text.toString())
        intent.putExtra(UDID_NUMBER, udidNumber)
        intent.putExtra(DATE_OF_BIRTH, binding.datePicker.text.toString())
        intent.putExtra(STATE, binding.stateSpinner.selectedItem.toString())
        intent.putExtra(DISTRICT, binding.districtSpinner.selectedItem.toString())
        intent.putExtra(MOBILE_NO, mobileNo.toString())
        startActivity(intent)

//        FirebaseDatabase.getInstance().getReference("${Constants.UDID_NO_LIST}/${udidNumber}")
//            .get().addOnSuccessListener {
//                if(it.exists()){
//                    //TODO: Show bottom sheet error dialog
//                    Toast.makeText(this , "UDID number alreaday exist!!", Toast.LENGTH_LONG)
//                        .show()
//                } else{
//                    val intent = Intent(applicationContext, UploadProfileActivity::class.java)
//
//                }
//            }
    }

    private fun openBrowser(){
        val url = "https://www.swavlambancard.gov.in/pwd/application"
        val intent = Intent(Intent.ACTION_VIEW , Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setPackage("com.android.chrome")
        try {
            applicationContext.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            applicationContext.startActivity(intent)
        }
    }

    private fun initDatePicker(){
       val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            val date = makeDateString(day , month, year)
            binding.datePicker.text = date.toString()
        }
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val style = AlertDialog.THEME_HOLO_LIGHT
        datePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)
    }

    private fun openDatePicker(view: View){
        datePickerDialog.show()
    }

    private fun makeDateString(day : Int , month : Int , year : Int) : String{
        return getMonthFormat(month+1) + " " + day + " " + year
    }

    private fun getMonthFormat(month: Int): String {
        when(month){
            1->return "JAN"
            2->return "FEB"
            3->return "MAR"
            4->return "APR"
            5->return "MAY"
            6->return "JUNE"
            7->return "JULY"
            8->return "AUG"
            9->return "SEPT"
            10->return "OCT"
            11->return "NOV"
            12->return "DEC"
            else -> return "JAN"
        }
    }

    companion object{
        const val NAME = "NAME"
        const val MOBILE_NO = "MOBILE_NO"
        const val UDID_NUMBER = "UDID_NUMBER"
        const val DATE_OF_BIRTH = "DATE_OF_BIRTH"
        const val STATE = "STATE"
        const val DISTRICT = "DISTRICT"
    }

}
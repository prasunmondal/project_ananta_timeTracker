package com.prasunmondal.ananta.timetrack.Utility.PostToSheet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.ananta.timetrack.ChooseInputMethod
import com.prasunmondal.ananta.timetrack.R
import com.prasunmondal.ananta.timetrack.files.ReadCustomerDetails
import com.prasunmondal.ananta.timetrack.values.Customer
import com.prasunmondal.lib.android.downloadfile.DownloadableFiles
import kotlinx.android.synthetic.main.activity_select_customer.*


class SelectCustomer : AppCompatActivity() {

    private lateinit var dropdown: Spinner
    private var selectCustomerLabel = "Please Select a Name"
    private lateinit var breakdownSheet: DownloadableFiles
    private lateinit var customerList: MutableList<Customer>

    private val LABEL_NAME = "Name : "
    private val LABEL_CONTACT_NO = "Phone No. "
    private val LABEL_ADDRESS = "Address: "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_customer)
        setSupportActionBar(toolbar)
        dropdown = findViewById(R.id.custNameSelection)

        customerList = mutableListOf()

        breakdownSheet = DownloadableFiles(
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vQSu2ZUWS9RHAdkeOH-vzno6mwTBYhN6gL1lXeREG3OQGrQXpzJ9lcpdYzKWB7f7AgawNKsPiusxGDi/pub?gid=1400723064&single=true&output=csv",
            "",
            "custDetails.csv",
            "TimeTrack",
            "fetching transaction details", ::onCustomerListDownlaodComplete,
            applicationContext
        )

        dropdown.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                updateDisplayValues()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        breakdownSheet.download()
    }

    private fun onCustomerListDownlaodComplete() {
        populateAllCustomerDetails()
        loadDataToSpinner()
        updateDisplayValues()
    }
    private fun populateAllCustomerDetails() {
        customerList = ReadCustomerDetails().populateCustomerList(breakdownSheet)

    }

    private fun loadDataToSpinner() {
        var geeks = mutableListOf("")
        geeks = ReadCustomerDetails().getListOfValues(breakdownSheet, 1)

        val dataAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, geeks)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = dataAdapter
    }

    fun onClickInfoSaveButton(view: View) {
        println("clicked - goToCountDown")
        saveFormData()
        goToChooseInputMethod()
    }

    private fun updateDisplayValues() {
        val nameView = findViewById<TextView>(R.id.custNameView)
        val contactView = findViewById<TextView>(R.id.custContactView)
        val addressView = findViewById<TextView>(R.id.custAddressView)

        if(customerList.isEmpty()) {
            nameView.text = "Downloading data..."
            contactView.text = ""
            addressView.text = ""
            return
        }

        if(isCustomerSelectionValid()) {
            val nameInput = dropdown.selectedItem.toString()
            for (c in customerList) {
                if(nameInput == c.name) {
                    nameView.text = LABEL_NAME + c.name
                    contactView.text = LABEL_CONTACT_NO + c.phoneNumber
                    addressView.text = LABEL_ADDRESS + c.address
                }
            }
        } else {
            nameView.text = selectCustomerLabel
            contactView.text = ""
            addressView.text = ""
        }
    }

    private fun saveFormData() {

    }

    private fun goToChooseInputMethod() {
        val i = Intent(this@SelectCustomer, ChooseInputMethod::class.java)
        startActivity(i)
        finish()
    }

    private fun isCustomerSelectionValid(): Boolean {
        return (dropdown.selectedItem.toString() != (selectCustomerLabel))
    }
}

package com.prasunmondal.ananta.timetrack.Utility.PostToSheet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.ananta.timetrack.ChooseInputMethod
import com.prasunmondal.ananta.timetrack.R
import com.prasunmondal.ananta.timetrack.files.ReadCustomerDetails
import com.prasunmondal.ananta.timetrack.utils.LogActions
import com.prasunmondal.ananta.timetrack.values.Customer
import com.prasunmondal.ananta.timetrack.values.SessionData
import com.prasunmondal.lib.android.downloadfile.DownloadableFiles
import kotlinx.android.synthetic.main.activity_select_customer.*


class SelectCustomer : AppCompatActivity() {

    private lateinit var dropdown: Spinner
    private var selectCustomerLabel = "Please Select a Name"
    private lateinit var breakdownSheet: DownloadableFiles
    private lateinit var customerList: MutableList<Customer>

    private val LABEL_NAME = ""
    private val LABEL_CONTACT_NO = "\uD83D\uDCDE  "
    private val LABEL_ADDRESS = "\uD83D\uDCCC  "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_customer)
        setSupportActionBar(toolbar)
        dropdown = findViewById(R.id.custNameSelection)

        showDownloading()
        customerList = mutableListOf()

        ToSheets.logs.post(listOf(LogActions.DOWNLOAD_START.name, "Metadata"), this)
        breakdownSheet = DownloadableFiles(
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vQSu2ZUWS9RHAdkeOH-vzno6mwTBYhN6gL1lXeREG3OQGrQXpzJ9lcpdYzKWB7f7AgawNKsPiusxGDi/pub?gid=1400723064&single=true&output=csv",
            "",
            "custDetails.csv",
            "TimeTrack",
            "fetching transaction details", ::onCustomerListDownlaodComplete,
            applicationContext
        )
        breakdownSheet.download()
    }

    private fun showDownloading() {
        var geeks = mutableListOf("Downloading")
        val dataAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, geeks)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = dataAdapter
        bottomToolbarConfig("Downloading...",false)
        findViewById<View>(R.id.showDetailsPanel).visibility = View.INVISIBLE
    }

    private fun onCustomerListDownlaodComplete() {
        ToSheets.logs.post(listOf(LogActions.DOWNLOAD_COMPLETE.name, "Metadata"), this)
        bottomToolbarConfig("Select Name", true)
        populateAllCustomerDetails()
        loadDataToSpinner()
        updateDisplayValues()

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

    fun onClickInfoSaveButton() {
        println("clicked - goToCountDown")
        if(getSelectedCustomer()==null) {
            ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Select Customer - Invalid Selection"), this)
            Toast.makeText(this, "Select a valid name", Toast.LENGTH_SHORT).show()
        } else {
            saveFormData()
            goToChooseInputMethod()
        }
    }

    private fun updateDisplayValues() {
        val nameView = findViewById<TextView>(R.id.custNameView)
        val contactView = findViewById<TextView>(R.id.custContactView)
        val addressView = findViewById<TextView>(R.id.custAddressView)

        if(isCustomerSelectionValid()) {
            val nameInput = dropdown.selectedItem.toString()
            for (c in customerList) {
                if(nameInput == c.name) {
                    nameView.text = LABEL_NAME + c.name
                    contactView.text = LABEL_CONTACT_NO + c.phoneNumber
                    addressView.text = LABEL_ADDRESS + c.address

                    bottomToolbarConfig("Next", true)
                    findViewById<View>(R.id.showDetailsPanel).visibility = View.VISIBLE
                }
            }
        } else {
            bottomToolbarConfig("Select Name", true)
            nameView.text = ""
            contactView.text = ""
            addressView.text = ""
            findViewById<View>(R.id.showDetailsPanel).visibility = View.INVISIBLE
        }
    }

    private fun saveFormData() {
        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Select Customer - " + getSelectedCustomer()!!.name), this)
        SessionData.Singleton.instance.currentCustomer = getSelectedCustomer()!!
    }

    private fun goToChooseInputMethod() {
        val i = Intent(this@SelectCustomer, ChooseInputMethod::class.java)
        startActivity(i)
    }

    private fun isCustomerSelectionValid(): Boolean {
        return (dropdown.selectedItem.toString() != (selectCustomerLabel) && getSelectedCustomer() != null)
    }

    private fun getSelectedCustomer(): Customer? {
        val nameInput = dropdown.selectedItem.toString()
        for (c in customerList) {
            if(nameInput == c.name) {
                return c
            }
        }
        return null
    }

    private fun bottomToolbarConfig(text: String, clickEnabled: Boolean) {
        var btn = findViewById<Button>(R.id.btn_selectCust_next)
        btn.text = text
        if(clickEnabled) {
            btn.setOnClickListener {
                onClickInfoSaveButton()
            }
        } else {
            btn.setOnClickListener { }
        }
    }
}

package com.teamdev.mykasir.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.teamdev.mykasir.ProfilActivity
import com.teamdev.mykasir.R
import com.teamdev.mykasir.base.BaseActivity

class SettingActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_setting)
        setToolbarTitle("Pengaturan")
        setCheckedNavigationItem(R.id.nav_pengaturan)

        val itemProfil = findViewById<LinearLayout>(R.id.itemProfil)
        itemProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }

        val itemCp = findViewById<LinearLayout>(R.id.itemCp)
        itemCp.setOnClickListener {
            val intent = Intent(this, ChangePwActivity::class.java)
            startActivity(intent)
        }


    }

}

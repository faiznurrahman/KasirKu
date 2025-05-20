package com.teamdev.mykasir.ui.setting

import android.os.Bundle
import com.teamdev.mykasir.R
import com.teamdev.mykasir.base.BaseActivity

class SettingActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_setting)
        setToolbarTitle("Pengaturan")
        setCheckedNavigationItem(R.id.nav_pengaturan)


    }

}

package com.merxury.blocker.ui.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.jaeger.library.StatusBarUtil
import com.merxury.blocker.R
import com.merxury.blocker.adapter.FragmentAdapter
import com.merxury.blocker.base.IActivityView
import com.merxury.blocker.ui.settings.SettingsActivity
import com.merxury.blocker.util.setupActionBar
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity(), IActivityView {
    private lateinit var drawer: Drawer
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Set up toolbar
        setupActionBar(R.id.toolbar) {
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
            setDisplayHomeAsUpEnabled(true)
        }
        setupDrawerContent(savedInstanceState)
        setupViewPager(app_viewpager)
        findViewById<TabLayout>(R.id.app_kind_tabs).apply {
            setupWithViewPager(app_viewpager)
            setupTab(this)
        }

    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = FragmentAdapter(supportFragmentManager)
        adapter.addFragment(ApplicationListFragment.newInstance(false), getString(R.string.third_party_app_tab_text))
        adapter.addFragment(ApplicationListFragment.newInstance(true), getString(R.string.system_app_tab_text))
        viewPager.adapter = adapter
    }

    private fun setupDrawerContent(savedInstanceState: Bundle?) {
        val item1 = PrimaryDrawerItem().withIdentifier(1).withName(R.string.app_list_title).withIcon(R.drawable.ic_list)
        val item2 = SecondaryDrawerItem().withIdentifier(2).withName(R.string.action_settings).withIcon(R.drawable.ic_settings)
        drawer = DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withSavedInstance(savedInstanceState)
                .withActionBarDrawerToggleAnimated(true)

                .addDrawerItems(
                        item1,
                        DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    when (drawerItem?.identifier) {
                        1L -> startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                        2L -> startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
                    }
                    false
                }
                .withSelectedItem(1L)
                .withCloseOnClick(true)
                .build()
        drawerLayout = drawer.drawerLayout

    }

    private fun setupTab(tabLayout: TabLayout) {
        changeColor(getBackgroundColor(0))
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.md_white_1000))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                changeBackgroundColor(tabLayout, tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawer.openDrawer()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeColor(color: Int) {
        toolbar.setBackgroundColor(color)
        app_kind_tabs.setBackgroundColor(color)
        StatusBarUtil.setColorForDrawerLayout(this, drawerLayout, color, com.merxury.blocker.constant.Constant.STATUS_BAR_ALPHA)
        findViewById<View>(com.jaeger.library.R.id.statusbarutil_translucent_view).setBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(ActivityManager.TaskDescription(null, null, color))
        }
    }


    private fun changeBackgroundColor(tabLayout: TabLayout, tab: TabLayout.Tab) {
        val colorFrom: Int
        if (tabLayout.background != null) {
            colorFrom = (tabLayout.background as ColorDrawable).color
        } else {
            colorFrom = ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        val colorTo = getBackgroundColor(tab.position)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animation ->
            val color = animation.animatedValue as Int
            changeColor(color)
        }
        colorAnimation.duration = 500
        colorAnimation.start()
    }

    override fun getBackgroundColor(tabPosition: Int): Int {
        return when (tabPosition) {
            0 -> ContextCompat.getColor(this, R.color.colorPrimary)
            1 -> ContextCompat.getColor(this, R.color.md_red_500)
            else -> ContextCompat.getColor(this, R.color.md_grey_700)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        }
    }
}

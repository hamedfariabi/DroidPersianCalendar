package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DeviceInfoRowBinding
import com.byagowi.persiancalendar.databinding.FragmentDeviceInfoBinding
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInformationFragment : DaggerFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    private var clickCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return FragmentDeviceInfoBinding.inflate(inflater, container, false).run {
            mainActivityDependency.mainActivity.setTitleAndSubtitle(getString(R.string.device_info), "")

            circularRevealFromMiddle(circularReveal)

            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(mainActivityDependency.mainActivity)
                addItemDecoration(DividerItemDecoration(mainActivityDependency.mainActivity, LinearLayoutManager.VERTICAL))
                adapter = DeviceInfoAdapter(mainActivityDependency.mainActivity, root)
            }

            bottomNavigation.run {
                menu.run {
                    add(Build.VERSION.RELEASE)
                    getItem(0).setIcon(R.drawable.ic_developer)

                    add("API " + Build.VERSION.SDK_INT)
                    getItem(1).setIcon(R.drawable.ic_settings)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        add(Build.SUPPORTED_ABIS[0])
                    } else {
                        add(Build.CPU_ABI)
                    }
                    getItem(2).setIcon(R.drawable.ic_motorcycle)

                    add(Build.MODEL)
                    getItem(3).setIcon(R.drawable.ic_device_information)
                }
                labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
                setOnNavigationItemSelectedListener {
                    // Easter egg
                    if (++clickCount % 10 == 0) {
                        BottomSheetDialog(mainActivityDependency.mainActivity).apply {
                            setContentView(IndeterminateProgressBar(mainActivityDependency.mainActivity).apply {
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700)
                            })
                        }.show()
                    }
                    true
                }
            }

            root
        }
    }
}

class DeviceInfoAdapter constructor(activity: Activity, private val rootView: View)
    : ListAdapter<DeviceInfoAdapter.DeviceInfoItem, DeviceInfoAdapter.ViewHolder>(DeviceInfoDiffCallback()) {
    private val deviceInfoItemsList = ArrayList<DeviceInfoItem>()

    data class DeviceInfoItem(val title: String, val content: String?, val version: String)

    class DeviceInfoDiffCallback : DiffUtil.ItemCallback<DeviceInfoItem>() {
        override fun areItemsTheSame(oldItem: DeviceInfoItem, newItem: DeviceInfoItem): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: DeviceInfoItem, newItem: DeviceInfoItem): Boolean = oldItem == newItem
    }

    init {
        deviceInfoItemsList.apply {
            addIfNotNull("Screen Resolution",
                    getScreenResolution(activity.windowManager),
                    "")

            addIfNotNull("Android Version",
                    Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT.toString())

            addIfNotNull("Manufacturer",
                    Build.MANUFACTURER, ""
            )

            addIfNotNull(
                    "Brand",
                    Build.BRAND, ""
            )

            addIfNotNull(
                    "Model",
                    Build.MODEL, ""
            )

            addIfNotNull(
                    "Product",
                    Build.PRODUCT, ""
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Build.SUPPORTED_ABIS.forEachIndexed { index, abi ->
                    addIfNotNull("Instruction CPU ${index + 1}",
                            abi, "")
                }
            } else {
                addIfNotNull(
                        "Instruction CPU 1",
                        Build.CPU_ABI, ""
                )

                addIfNotNull(
                        "Instruction CPU 2",
                        Build.CPU_ABI2, ""
                )
            }

            addIfNotNull(
                    "Instruction Architecture",
                    Build.DEVICE, ""
            )

            addIfNotNull(
                    "Android Id",
                    Build.ID, ""
            )

            addIfNotNull(
                    "Board",
                    Build.BOARD, ""
            )

            addIfNotNull(
                    "Radio Firmware Version",
                    Build.getRadioVersion(), ""
            )

            addIfNotNull(
                    "Build User",
                    Build.USER, ""
            )

            addIfNotNull(
                    "Host",
                    Build.HOST, ""
            )

            addIfNotNull(
                    "Display",
                    Build.DISPLAY, ""
            )

            addIfNotNull(
                    "Device Fingerprints",
                    Build.FINGERPRINT, ""
            )
        }

        // If one wants to add kernel related cpu information
        //   try {
        //       for (File fileEntry : new File("/sys/devices/system/cpu/cpu0/cpufreq/").listFiles()) {
        //           if (fileEntry.isDirectory()) continue;
        //           try {
        //               deviceInfoItemsList.add(new DeviceInfoItem(
        //                       fileEntry.getAbsolutePath(),
        //                       Utils.readStream(new FileInputStream(fileEntry)),
        //                       null
        //               ));
        //           } catch (Exception e) {
        //               e.printStackTrace();
        //           }
        //       }
        //   } catch (Exception e) {
        //       e.printStackTrace();
        //   }
    }

    private fun addIfNotNull(title: String, content: String?, version: String) {
        deviceInfoItemsList.add(DeviceInfoItem(
                title,
                content ?: "Unknown", version
        ))
    }

    private fun getScreenResolution(wm: WindowManager): String =
            String.format(Locale.ENGLISH, "%d*%d pixels",
                    wm.defaultDisplay.width, wm.defaultDisplay.height)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DeviceInfoRowBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = deviceInfoItemsList.size

    inner class ViewHolder(private val mBinding: DeviceInfoRowBinding) : RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {
        private var mPosition = 0

        init {
            mBinding.root.setOnClickListener(this)
        }

        fun bind(position: Int) {
            mPosition = position
            with(deviceInfoItemsList[position]) {
                mBinding.title.text = title
                mBinding.content.text = content
                mBinding.version.text = version
            }
        }

        override fun onClick(v: View?) {
            deviceInfoItemsList[mPosition].apply {
                copyToClipboard(rootView, title, content)
            }
        }
    }
}

package com.k5.omsetku.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.k5.omsetku.R
import com.k5.omsetku.features.product.Product
import com.k5.omsetku.features.sales.Sales
import com.k5.omsetku.features.sales.SalesAdapter
import com.k5.omsetku.fragment.loadfragment.LoadFragment
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SalesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SalesFragment : Fragment(), SalesAdapter.OnItemActionListener {
    private lateinit var recyclerViewSales: RecyclerView
    private lateinit var salesAdapter: SalesAdapter
    private lateinit var saleList: ArrayList<Sales>

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewSales = view.findViewById(R.id.rv_sales)

        saleList = ArrayList()
        saleList.add(Sales("1", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))
        saleList.add(Sales("2", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))
        saleList.add(Sales("3", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))
        saleList.add(Sales("4", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))
        saleList.add(Sales("5", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))
        saleList.add(Sales("6", "Anonym", "INV-2025-04-19-001", "12 April 2025, 14:30", 12000000))

        salesAdapter = SalesAdapter(saleList, this)
        recyclerViewSales.adapter = salesAdapter

    }

    override fun onSalesDetailsClicked(
        sales: Sales,
        position: Int
    ) {
        LoadFragment.loadChildFragment(parentFragmentManager, R.id.host_fragment ,SalesDetailsFragment())
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SalesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SalesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.adapters.CardSliderAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Cards
import com.heena.user.models.MyResponse
import com.heena.user.models.ViewCardResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_my_cards.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.abs

class MyCardsFragment : Fragment() {
    var bundle  = Bundle()
    lateinit var CardSliderAdapter: CardSliderAdapter
    var cardsList = ArrayList<Cards>()
    private var mView : View?=null
    val fragmentsList = ArrayList<Fragment>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView =  inflater.inflate(R.layout.fragment_my_cards, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    viewCards()
                    noInternetDialog.dismiss()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Cards Fragment")
        }else{
            viewCards()
        }


        mView!!.tv_add_new_card.setSafeOnClickListener {
            //findNavController().navigate(R.id.mycardsFragment_to_addNewCardFragment)
            val bundle = Bundle()
            bundle.putString("title", "addCards")
            findNavController().navigate(R.id.cmsFragment, bundle)
        }

        mView!!.tv_delete_card.setSafeOnClickListener {
            val deleteCardDialog = AlertDialog.Builder(requireContext())
            deleteCardDialog.setCancelable(false)
            deleteCardDialog.setTitle(requireContext().getString(R.string.delete_address))
            deleteCardDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_card))
            deleteCardDialog.setPositiveButton(requireContext().getString(R.string.delete)
            ) { dialog, _ ->
                val pos = mView!!.vpCards.currentItem
                deleteCard(pos)
                dialog!!.dismiss()
            }
            deleteCardDialog.setNegativeButton(requireContext().getString(R.string.cancel)
            ) { dialog, _ -> dialog!!.cancel() }
            deleteCardDialog.show()
        }
    }

    private fun deleteCard(pos: Int) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_view_cards.visibility= View.VISIBLE
        val builder = createBuilder(arrayOf("user_id", "name", "number","cvv", "expiry_date", "type", "card_id"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                cardsList[pos].name,
                cardsList[pos].number,
                cardsList[pos].cvv,
                cardsList[pos].expiry_date, "1", cardsList[pos].id.toString()))
        val call = apiInterface.addDeleteCard(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(
                call: Call<MyResponse?>,
                response: Response<MyResponse?>
            ) {
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.cardsFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            fragmentsList.removeAt(pos)
                            cardsList.removeAt(pos)
                            if (fragmentsList.size==0){
                                mView!!.tv_no_cards_found.visibility = View.VISIBLE
                                mView!!.tv_delete_card.visibility = View.GONE
                            }else{
                                mView!!.tv_no_cards_found.visibility = View.GONE
                                mView!!.tv_delete_card.visibility = View.VISIBLE
                            }
                            CardSliderAdapter.notifyDataSetChanged()
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.cardsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())

                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.cardsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun viewCards() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_view_cards.visibility= View.VISIBLE
        val call = apiInterface.showCards(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<ViewCardResponse?> {
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            mView!!.tv_no_cards_found.visibility = View.GONE
                            cardsList.clear()
                            fragmentsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!
                            if (cardsList.size==0){
                                mView!!.tv_delete_card.visibility = View.GONE
                                mView!!.tv_no_cards_found.visibility = View.VISIBLE
                            }else{
                                mView!!.tv_delete_card.visibility = View.VISIBLE
                                mView!!.tv_no_cards_found.visibility = View.GONE
                            }
                            for (i in 0 until cardsList.size){
                                fragmentsList.add(CardSliderFragment(cardsList[i]))
                            }

                            CardSliderAdapter = CardSliderAdapter(requireActivity(), fragmentsList)
                            mView!!.vpCards.adapter =  CardSliderAdapter
                            // Disable clip to padding
                            mView!!.vpCards.setClipChildren(false)
                            mView!!.vpCards.setClipToPadding(false)
                            mView!!.vpCards.setOffscreenPageLimit(3)

                            mView!!.vpCards.setPageTransformer{ page: View, position: Float ->
                                page.scaleY = 1 - (0.14f * abs(position))
                            }

                        }else{
                            mView!!.tv_no_cards_found.visibility = View.VISIBLE
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.cardsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ViewCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.cardsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    override fun onResume() {
        super.onResume()
        viewCards()
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}
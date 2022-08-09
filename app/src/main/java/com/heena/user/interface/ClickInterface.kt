package com.heena.user.`interface`

import android.view.View
import com.heena.user.models.AddressItem
import com.heena.user.models.Content

interface ClickInterface {
    interface OnRecyclerItemClick{
        fun OnClickAction(position : Int)
    }

    interface OnServicesItemClick{
        fun OnClickAction(position : Int)
        fun onLikeAction(position: Int, itemView:View)
        fun onDislikeAction(position: Int, itemView:View)
        fun onProfileClick(position: Int)
    }

    interface onOfferItemClick{
        fun OnClickAction(position : Int)
        fun onLikeAction(position: Int, itemView:View)
        fun onDislikeAction(position: Int, itemView:View)
    }

    interface ClickPosInterface{
        fun clickPostion(pos:Int, type:String)
    }

    interface OnBookServiceClick{
        fun OnBookService()
        fun onLikeAction(position: Int, likeStatus : Boolean, itemView: View)
        fun onDislikeAction(position: Int, unLikeStatus : Boolean, itemView: View)
    }

    interface OnAddAddressClick{
        fun OnAddAddress(position: Int, addressItem: AddressItem)
    }

    interface OnAddressClick{
        fun OnAddress()
    }

    interface OnCalendarClick{
        fun OnCalendar()
    }

    interface OnAvailableNaqashaClick{
        fun OnAvailableNaqasha()
    }

    interface OnCancelServiceClick{
        fun OnCancelService(booking_id: Int?, rsn_for_cancellation: String)
    }

    interface OnRescheduleServiceClick{
        fun OnRescheduleService()
    }

    interface OnChatWithAdminClick{
        fun OnChatWithAdmin()
    }


    interface OnAddressItemClick{
        fun editAdddress(position: Int)
        fun deleteAddress(position: Int)
    }

    interface onSaveAppointmentDateTimeCallback{
        fun SaveAppointmentDateTimeCallback(hashMap : HashMap<String, String>)
    }

    interface onFavItemClick{
        fun delFav(position: Int)
        fun openFav(position: Int)
    }

    interface setAddressClick{
        fun setAddress(myaddressItem: AddressItem?)
    }

    interface mainhelpCategoryClicked{
        fun mainHelpCategory(position: Int)
    }

    interface subhelpCategoryClicked{
        fun subHelpCategory(position: Int, content: ArrayList<Content>)
    }

    interface onReviewSubmit{
        fun reviewSubmit(reviewMessage: String)
    }
}
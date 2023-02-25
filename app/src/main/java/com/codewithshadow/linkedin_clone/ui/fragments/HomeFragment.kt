package com.codewithshadow.linkedin_clone.ui.fragments

import com.google.firebase.auth.FirebaseUser
import com.codewithshadow.linkedin_clone.models.PostModel
import com.codewithshadow.linkedin_clone.adapters.PostAdapter
import com.todkars.shimmer.ShimmerRecyclerView
import com.google.firebase.database.DatabaseReference
import com.codewithshadow.linkedin_clone.models.StoryModel
import com.codewithshadow.linkedin_clone.adapters.StoryAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.codewithshadow.linkedin_clone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codewithshadow.linkedin_clone.constants.Constants
import com.codewithshadow.linkedin_clone.models.UserModel
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class HomeFragment : Fragment() {
    var user: FirebaseUser? = null
    var list: MutableList<PostModel?>? = null
    var adapter: PostAdapter? = null
    var recyclerView: ShimmerRecyclerView? = null
    var recyclerViewStory: ShimmerRecyclerView? = null
    var ref: DatabaseReference? = null
    val firestore = FirebaseFirestore.getInstance()
    var storyModelList: MutableList<StoryModel?>? = null
    var storyAdapter: StoryAdapter? = null

    private lateinit var followingList: MutableList<String?>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.post_recycler)
        recyclerViewStory = view.findViewById(R.id.story_recycler)
        user = FirebaseAuth.getInstance().currentUser
        ref = FirebaseDatabase.getInstance().reference
        ref!!.keepSynced(true)
        list = ArrayList()
        storyModelList = ArrayList()
        followingList = ArrayList()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Post RecyclerView
        recyclerView!!.showShimmer()
        adapter = PostAdapter(context, list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.isNestedScrollingEnabled = false

        //Story RecyclerView
        storyAdapter = StoryAdapter(activity, storyModelList)
        recyclerViewStory!!.setHasFixedSize(true)
        recyclerViewStory!!.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerViewStory!!.adapter = storyAdapter
        recyclerViewStory!!.isNestedScrollingEnabled = false

        //Functions
        Read_Posts()
        GetAllUsersId()
    }

    //----------------------------------Read Posts--------------------------------//
    private fun Read_Posts() {
        recyclerView!!.hideShimmer()

        ref!!.child(Constants.ALL_POSTS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list!!.clear()
                    for (dataSnapshot in snapshot.children) {
                        val model = dataSnapshot.child(Constants.INFO).getValue(
                            PostModel::class.java
                        )
                        list!!.add(model)
                    }
                    Collections.reverse(list)
                    recyclerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //--------------------------------Get All Users Id--------------------------------//
    private fun GetAllUsersId() {
        followingList = ArrayList()
        ref!!.child(Constants.USER_CONSTANT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList.clear()
                var model: UserModel? = null
                for (dataSnapshot in snapshot.children) {
                    model = dataSnapshot.child(Constants.INFO).getValue(
                        UserModel::class.java
                    )
                    assert(model != null)
                    if (model!!.key != user!!.uid) {
                        followingList.add(dataSnapshot.key)
                    }
                }
                readStory()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //-----------------------------Read Story------------------------//
    private fun readStory() {
        ref!!.child(Constants.STORY).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                storyModelList!!.clear()
                storyModelList!!.add(
                    StoryModel(
                        "", 0, 0, FirebaseAuth.getInstance().currentUser!!
                            .uid, "", ""
                    )
                )
                for (id in followingList!!) {
                    var countStory = 0
                    var storyModel: StoryModel? = null
                    for (snapshot2 in snapshot.child(id!!).children) {
                        storyModel = snapshot2.getValue(StoryModel::class.java)
                        if (timeCurrent > storyModel!!.timeStart && timeCurrent < storyModel.timeEnd) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        storyModelList!!.add(storyModel)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
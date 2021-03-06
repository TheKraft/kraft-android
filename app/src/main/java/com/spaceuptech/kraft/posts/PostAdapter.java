package com.spaceuptech.kraft.posts;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.spaceuptech.clientapi.ClientApi;
import com.spaceuptech.kraft.DataService;
import com.spaceuptech.kraft.DatabaseHelper;
import com.spaceuptech.kraft.R;
import com.spaceuptech.kraft.data.Post;
import com.spaceuptech.kraft.profile.ProfileActivity;
import com.spaceuptech.kraft.utility.Conversions;
import com.spaceuptech.kraft.utility.TimeStamp;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> posts;
    private Context context;
    private Gson gson = new Gson();
    private DatabaseHelper databaseHelper;

    PostAdapter(Context context, DatabaseHelper databaseHelper, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.databaseHelper = databaseHelper;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = posts.get(position);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sessionId", DataService.SESSION_ID);
        jsonObject.addProperty("postId", post.postId);
        ClientApi.call(context, DataService.ENGINE_POST, DataService.REQUEST_IMPRESSION_POST, gson.toJson(jsonObject).getBytes());

        holder.textViewAuthorName.setText(post.userName);
        holder.textViewAuthorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("user-id", post.userId);
                intent.putExtra("user-name", post.userName);
                intent.putExtra("user-img", post.userImg);
                context.startActivity(intent);
            }
        });
        holder.textViewTime.setText(TimeStamp.getTimeElapsed(Conversions.getTimeFromUUID(UUID.fromString(post.postId))));
        holder.textViewContent.setText(post.content);
        if (post.likes < 1) {
            holder.textViewLikesCounter.setVisibility(View.GONE);
        } else {
            holder.textViewLikesCounter.setVisibility(View.VISIBLE);
            holder.textViewLikesCounter.setText(String.valueOf(post.likes));
        }
        Glide.with(context).load(post.userImg).into(holder.circleImageViewProfileAuthor);
        // TODO If no image then show the background containing first letter of User
        holder.imageButtonActionLikePost.setImageResource((databaseHelper.checkIfPostLiked(post.postId)) ? R.drawable.ic_favourite_red : R.drawable.ic_favourite);
        holder.imageButtonActionLikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean liked = databaseHelper.checkIfPostLiked(post.postId);
                holder.onClickAnimate(view);
                holder.imageButtonActionLikePost.setImageResource(!liked ? R.drawable.ic_favourite_red : R.drawable.ic_favourite);
                if (liked) {
                    post.likes--;
                    if (post.likes == 0)
                        holder.textViewLikesCounter.setVisibility(View.GONE);
                } else {
                    post.likes++;
                    if (post.likes > 0)
                        holder.textViewLikesCounter.setVisibility(View.VISIBLE);
                }
                holder.textViewLikesCounter.setText(String.valueOf(post.likes));

                //TODO Animations and sound effect on clicking like
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("sessionId", DataService.SESSION_ID);
                jsonObject.addProperty("postId", post.postId);
                jsonObject.addProperty("like", !liked);
                ClientApi.call(context, DataService.ENGINE_POST, DataService.REQUEST_LIKE_POST, gson.toJson(jsonObject).getBytes());
            }
        });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAuthorName, textViewTime, textViewContent, textViewLikesCounter;
        CircleImageView circleImageViewProfileAuthor;
        ImageButton imageButtonActionLikePost;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewAuthorName = (TextView) itemView.findViewById(R.id.lblAuthorNamePost);
            textViewTime = (TextView) itemView.findViewById(R.id.lblTimePost);
            textViewContent = (TextView) itemView.findViewById(R.id.lblContentPost);
            textViewLikesCounter = (TextView) itemView.findViewById(R.id.lblLikeCounterPost);
            circleImageViewProfileAuthor = (CircleImageView) itemView.findViewById(R.id.imgUserPost);
            imageButtonActionLikePost = (ImageButton) itemView.findViewById(R.id.btnLikePost);
        }
        public void onClickAnimate(View view){
            ImageButton img = (ImageButton) view;
            PropertyValuesHolder scalex = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
            PropertyValuesHolder scaley = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(img, scalex, scaley);
            anim.setRepeatCount(1);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setDuration(100);
            anim.start();
        }
    }
}

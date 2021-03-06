package com.example.gumptionlabs;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class MyVideoAdapter extends FirestoreRecyclerAdapter<MyVideo, MyVideoAdapter.MyVideoHolder> {

    private OnItemClickListener listener;
    public MyVideoAdapter(@NonNull FirestoreRecyclerOptions<MyVideo> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyVideoHolder holder, int position, @NonNull MyVideo model) {
        holder.name_tv.setText(model.getVideo_Name());
        holder.timestamp_tv.setText(model.getTimestamp().toDate().toString());
        holder.length_tv.setText(String.valueOf(model.getLength()));
        holder.password_tv.setText(String.valueOf(model.getPassword()));
        holder.url_tv.setText(String.valueOf(model.getVideo_URL()));
    }

    @NonNull
    @Override
    public MyVideoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_video_item,viewGroup,false);
        return new MyVideoHolder(v);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class MyVideoHolder extends RecyclerView.ViewHolder {

        TextView name_tv;
        TextView timestamp_tv;
        TextView length_tv;
        TextView password_tv;
        TextView url_tv;


        public MyVideoHolder(@NonNull View itemView) {
            super(itemView);

            name_tv=itemView.findViewById(R.id.myVideo_name);
            timestamp_tv=itemView.findViewById(R.id.myVideo_timestamp);
            length_tv=itemView.findViewById(R.id.myVideo_length);
            password_tv=itemView.findViewById(R.id.myVideo_pwd);
            url_tv=itemView.findViewById(R.id.myVideo_url);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}

package com.fmsh.temperature.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fmsh.temperature.R;
import com.fmsh.temperature.listener.OnItemClickListener;
import com.fmsh.temperature.util.UIUtils;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wyj on 2018/7/9.
 */
public class RecyclerAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private int [] mImgId = {  R.mipmap.icon_grid_layout,
            R.mipmap.icon_grid_qq_face_view,
            R.mipmap.icon_grid_pull_refresh_layout,
            R.mipmap.icon_grid_empty_view,
            R.mipmap.icon_grid_botton_sheet,
            R.mipmap.icon_grid_radius_image_view,
            R.mipmap.setting1,
            R.mipmap.setting2,
            R.mipmap.nfc_instruct,

    };


    public static final int TYPE_HEADER = 0;

    public static final int TYPE_FOOTER = 1;

    public static final int TYPE_NORMAL = 2;
    private View mHeaderView;
    private View mFooterView;


    private List<String> mList = new ArrayList<>();
    public RecyclerAdapter(Context context) {
        this.mContext = context;
    }

    public void setList(List<String> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public View getHeaderView() {
        return mHeaderView;
    }
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }
    public View getFooterView() {
        return mFooterView;
    }
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount()-1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        }
        if(mFooterView != null && viewType == TYPE_FOOTER){
            return new FoogerViewHolder(mFooterView);
        }
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_im_fragment, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(mHeaderView != null &&mFooterView != null){
            if(position == 0){
                return;
            }else if (position == getItemCount() -1){
                return;
            }else {
                bindHolder(holder,position-1);
            }
        }else if(mHeaderView !=null &&mFooterView == null){
            if(position == 0){
                return;
            }else {
                bindHolder(holder,position-1);
            }
        }else if(mHeaderView == null &&mFooterView != null){
            if(position == getItemCount() -1){
                return;
            }else {

                bindHolder(holder,position);
            }
        }else {
            bindHolder(holder,position);
        }




    }

    public void bindHolder(RecyclerView.ViewHolder holder, final int position){
        ViewHolder vh = (ViewHolder) holder;
        String title = mList.get(position);
        vh.tvContent.setText(title);
        vh.ivIcon.setImageResource(mImgId[position]);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.itemClickListener(position);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mFooterView == null){
            return TYPE_NORMAL;
        }
        if (position == 0){
            //第一个item应该加载Header
            return TYPE_HEADER;
        }
        if (position == getItemCount()-1){
            //最后一个,应该加载Footer
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        if(mHeaderView == null && mFooterView == null){
            return mList.size();
        }else if(mHeaderView == null && mFooterView != null){
            return mList.size() + 1;
        }else if (mHeaderView != null && mFooterView == null){
            return mList.size() + 1;
        }else {
            return mList.size() + 2;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        ImageView ivIcon;
        @BindView(R.id.tvContent)
        TextView tvContent;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
    /**
     * 头部的ViewHolder
     */
    private class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 尾部的ViewHolder
     */
    private class FoogerViewHolder extends RecyclerView.ViewHolder
    {
        public FoogerViewHolder(View itemView) {
            super(itemView);
        }
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager)
        {
            /**
             * getSpanSize的返回值的意思是：position位置的item的宽度占几列
             * 比如总的是4列，然后头部全部显示的话就应该占4列，此时就返回4
             * 其他的只占一列，所以就返回1，剩下的三列就由后面的item来依次填充。
             */
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(mHeaderView != null && mFooterView != null)
                    {
                        if(position == 0)
                        {
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        }
                        else if(position == getItemCount() - 1) {
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        }
                        else
                        {
                            return 1;
                        }
                    }
                    else if(mHeaderView != null) {
                        if (position == 0) {
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        }
                        return 1;
                    }
                    else if(mFooterView != null)
                    {
                        if(position == getItemCount() - 1)
                        {
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        }
                        return 1;
                    }
                    return 1;
                }
            });
        }
    }
}

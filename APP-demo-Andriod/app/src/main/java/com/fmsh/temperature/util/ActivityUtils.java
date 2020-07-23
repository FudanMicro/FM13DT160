package com.fmsh.temperature.util;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by wyj on 2018/8/8.
 */
public class ActivityUtils {

    private static Stack<Activity> mActivities;
    public volatile static ActivityUtils instance = new ActivityUtils();

    private ActivityUtils(){
        mActivities = new Stack<>();
    }

    public void addActivity(Activity activity){
        mActivities.add(activity);
    }


    public Activity getCurrentActivity(){
        try {
            return mActivities.lastElement();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void finishAllActivity(){
        for (int i = 0; i <mActivities.size() ; i++) {
            if(mActivities.get(i)!= null){
                mActivities.get(i).finish();
            }
        }
        mActivities.clear();
    }


    public void finsishActivity(Activity activity){
        if(activity != null){
            mActivities.remove(activity);
            activity.finish();
        }
    }


    public void finishCurrentActivity(){
        Activity activity = mActivities.lastElement();
        finsishActivity(activity);
    }

    public void finishActivity(Class<?> cla){
       try {
           for (Activity activity: mActivities) {
               if(activity.getClass().equals(cla)){
                   activity.finish();
               }
           }

       }catch (Exception e){
           e.printStackTrace();
       }
    }


    public void removeActivity(Activity activity){
        if(activity != null)
            mActivities.remove(activity);
    }


    public void returnToActivity(Class<?> cla){
       while (mActivities.size() != 0){
           if(mActivities.peek().getClass() == cla){
               break;
           }else {
               finishActivity(cla);
           }
       }
    }


    public boolean isOpenActivity(Class<?> cla){
        if(mActivities != null){
            for (Activity activity: mActivities) {
                if(activity.getClass() == cla){
                    return true;
                }
            }
        }
        return false;
    }





}

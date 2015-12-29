package com.vilyever.androiddrawingview;

import android.content.Context;
import android.view.View;

/**
 * BaseController
 * ESB <com.vilyever.operationbar>
 * Created by vilyever on 2015/11/23.
 * Feature:
 */
public abstract class BaseController {
    final BaseController self = this;

    protected Context context;

    protected View rootView;



    /* #Constructors */
    public BaseController(Context context) {
        self.context = context;
    }
    
    /* #Overrides */    
    
    /* #Accessors */
    public Context getContext() {
        return context;
    }

    public View getRootView() {
        return rootView;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */


    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}
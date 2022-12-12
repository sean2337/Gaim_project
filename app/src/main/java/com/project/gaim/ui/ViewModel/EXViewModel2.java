package com.project.gaim.ui.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EXViewModel2 extends ViewModel {

    private MutableLiveData<double[][]> exModel;

    public EXViewModel2() {
        exModel = new MutableLiveData<>();
    }
    public void setExModel(double[][] exModel){
        this.exModel.postValue(exModel);
    }
    public MutableLiveData<double[][]> getExModel() {
        return exModel;
    }

}

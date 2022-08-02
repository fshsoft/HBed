package com.java.health.care.bed.present;

import com.java.health.care.bed.base.BasePresenter;
import com.java.health.care.bed.contract.user.Contract;

/**
 * @author fsh
 * @date 2022/08/02 09:14
 * @Description
 */
public class UserPresenter extends BasePresenter<Contract.IUserView> implements Contract.IUserPresenter {
    @Override
    public void refreshUser() {

    }
}

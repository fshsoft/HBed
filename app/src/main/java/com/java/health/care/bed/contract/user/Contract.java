package com.java.health.care.bed.contract.user;

import com.java.health.care.bed.base.IBaseView;
import com.java.health.care.bed.bean.User;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 14:12
 * @Description  用户信息
 */
public class Contract {

    public interface IUserModel{


        /**
         * 用户信息
         * @return 接口数据
         */

        Observable<User> refreshUser();
    }



    public interface IUserView extends IBaseView {

        /**
         * 用户信息
         * @param user 数据显示
         */
        void refreshUser(User user);
    }



    public interface IUserPresenter{


        /**
         * 用户信息
         * @param
         */
        void refreshUser();
    }
}

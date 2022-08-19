package com.java.health.care.bed.test;

import com.java.health.care.bed.base.BasePresenter;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author fsh
 * @date 2022/08/19 13:18
 * @Description
 */
public class HomeSquarePresenter extends BasePresenter<Contracts.IHomeSquareView> implements Contracts.IHomeSquarePresenter {
    Contracts.IHomeSquareModel iHomeSquareModel;
    public HomeSquarePresenter() {
        iHomeSquareModel = new HomeSquareModel();
    }
    @Override
    public void loadHomeSquareData(int pageNum) {
        if (isViewAttached() && pageNum == 0) {
            getView().onLoading();
        }
        iHomeSquareModel.loadHomeSquareData(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Article>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<Article> articleList) {
                        if (isViewAttached()) {
                            getView().loadHomeSquareData(articleList);
                            getView().onLoadSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (isViewAttached()) {
                            getView().onLoadFailed();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void refreshHomeSquareData(int pageNum) {
        iHomeSquareModel.refreshHomeSquareData(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Article>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<Article> articleList) {
                        if (isViewAttached()) {
                            getView().refreshHomeSquareData(articleList);
                            getView().onLoadSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (isViewAttached()) {
                            getView().onLoadFailed();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}

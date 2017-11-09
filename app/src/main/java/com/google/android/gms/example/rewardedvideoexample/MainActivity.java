/*
 * Copyright (C) 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.example.rewardedvideoexample;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * Main Activity. Inflates main activity xml and implements RewardedVideoAdListener.
 */
public class MainActivity extends Activity {
    private static final long COUNTER_TIME = 10;
    private static final int GAME_OVER_REWARD = 1;
    private static final String TIME_REMAINING_KEY = "TIME_REMAINING";
    private static final String COIN_COUNT_KEY = "COIN_COUNT";
    private static final String GAME_PAUSE_KEY = "IS_GAME_PAUSED";
    private static final String GAME_OVER_KEY = "IS_GAME_OVER";

    private int mCoinCount;
    private TextView mCoinCountText;
    private CountDownTimer mCountDownTimer;
    private boolean mGameOver;
    private boolean mGamePaused;
    private Button mRetryButton;
    private long mTimeRemaining;

    private RewardedVideoAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(),
                getString(R.string.admob_app_id));

        // Get reference to singleton RewardedVideoAd object
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);


        // Create the "retry" button, which starts a new game.
        mRetryButton = ((Button) findViewById(R.id.retry_button));
        mRetryButton.setVisibility(View.INVISIBLE);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        mCoinCountText = ((TextView) findViewById(R.id.coin_count_text));

        if (savedInstanceState == null) {
            mCoinCount = 0;
            mCoinCountText.setText("Coins: " + mCoinCount);

            startGame();
        }
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Toast.makeText(getBaseContext(),
                        "Ad loaded.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Toast.makeText(getBaseContext(),
                        "Ad opened.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoStarted() {
                Toast.makeText(getBaseContext(),
                        "Ad started.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Toast.makeText(getBaseContext(),
                        "Ad closed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Toast.makeText(getBaseContext(),
                        "Ad triggered reward.", Toast.LENGTH_SHORT).show();
                addCoins(rewardItem.getAmount());
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Toast.makeText(getBaseContext(),
                        "Ad left application.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Toast.makeText(getBaseContext(),
                        "Ad failed to load.", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mGamePaused = savedInstanceState.getBoolean(GAME_PAUSE_KEY);
        mGameOver = savedInstanceState.getBoolean(GAME_OVER_KEY);
        mTimeRemaining = savedInstanceState.getLong(TIME_REMAINING_KEY);
        mCoinCount = savedInstanceState.getInt(COIN_COUNT_KEY);
        mCoinCountText.setText("Coins: " + mCoinCount);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(GAME_PAUSE_KEY, mGamePaused);
        outState.putBoolean(GAME_OVER_KEY, mGameOver);
        outState.putLong(TIME_REMAINING_KEY, mTimeRemaining);
        outState.putInt(COIN_COUNT_KEY, mCoinCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
        pauseGame();
    }

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
        if (!mGameOver && mGamePaused) {
            resumeGame();
        }
        if (mGameOver) {
            mRetryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    private void pauseGame() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mGamePaused = true;
    }

    private void resumeGame() {
        createTimer(mTimeRemaining);
        mGamePaused = false;
    }

    private void addCoins(int coins) {
        mCoinCount = mCoinCount + coins;
        mCoinCountText.setText("Coins: " + mCoinCount);
    }

    private void startGame() {
        // Hide the retry button and start the timer.
        mRetryButton.setVisibility(View.INVISIBLE);
        createTimer(COUNTER_TIME);
        mGamePaused = false;
        mGameOver = false;
    }

    // Create the game timer, which counts down to the end of the level.
    private void createTimer(long time) {
        final TextView textView = ((TextView) findViewById(R.id.timer));
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(time * 1000, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                mTimeRemaining = ((millisUnitFinished / 1000) + 1);
                textView.setText("seconds remaining: " + mTimeRemaining);
            }

            @Override
            public void onFinish() {
                gameOver();
            }
        };
        mCountDownTimer.start();
    }

    private void gameOver() {
        final TextView textView = ((TextView) findViewById(R.id.timer));
        textView.setText("You Lose!");
        addCoins(GAME_OVER_REWARD);
        mRetryButton.setVisibility(View.VISIBLE);
        mGameOver = true;
    }
}

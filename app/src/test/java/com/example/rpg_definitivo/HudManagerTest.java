package com.example.rpg_definitivo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HudManagerTest {

    @Mock
    Context mockContext;
    @Mock
    Resources mockResources;
    @Mock
    Bitmap mockBitmap;

    @Before
    public void setUp() {
        when(mockContext.getResources()).thenReturn(mockResources);
    }

    @Test
    public void testGetHudFrameReturnsBitmap() {
        try (MockedStatic<BitmapFactory> mockedBitmapFactory = Mockito.mockStatic(BitmapFactory.class);
             MockedStatic<Bitmap> mockedBitmap = Mockito.mockStatic(Bitmap.class)) {
            
            mockedBitmapFactory.when(() -> BitmapFactory.decodeResource(any(Resources.class), anyInt())).thenReturn(mockBitmap);
            when(mockBitmap.getWidth()).thenReturn(500);
            when(mockBitmap.getHeight()).thenReturn(100);
            
            mockedBitmap.when(() -> Bitmap.createBitmap(any(Bitmap.class), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(mock(Bitmap.class));

            // Stress test the cache
            for (int i = 0; i < 100; i++) {
                HudManager.getHudFrame(mockContext, 100, 100, i % 100, 100);
            }
        }
    }
}

package com.yhy.http.pigeon.spring.starter.register;

import com.yhy.http.pigeon.spring.starter.annotation.EnablePigeon;
import com.yhy.http.pigeon.spring.starter.annotation.Pigeon;

import java.lang.annotation.Annotation;

/**
 * Created on 2021-11-03 14:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class PigeonAutoRegister extends AbstractPigeonAutoRegister {
    @Override
    public Class<? extends Annotation> enableAnnotation() {
        return EnablePigeon.class;
    }

    @Override
    public Class<? extends Annotation> pigeonAnnotation() {
        return Pigeon.class;
    }
}

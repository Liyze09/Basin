package net.liyze.basin.summer.jdbc.tx;

import net.liyze.basin.summer.annotation.Transactional;
import net.liyze.basin.summer.aop.AnnotationProxyBeanPostProcessor;

public class TransactionalBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {

}

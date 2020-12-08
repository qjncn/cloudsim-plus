/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.listeners;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

/**
 *
 * An interface to define Observers (Listeners) that listen to specific changes in
 * the state of a given observable object (Subject).
 * By this way, the EventListener gets notified when
 * the observed object has its state changed.
 *  定义观察者(监听器)的接口，侦听给定可观察对象(主题)状态的特定变化。
 * 通过这种方式，当被观察对象的状态发生改变时，EventListener会得到通知。
 *
 * <p>The interface was defined allowing the Subject object to have more than one state
 * to be observable. If the subject directly implements
 * this interface, it will allow only one kind of state change to be observable.
 * If the Subject has multiple state changes to be observed,
 * it can define multiple EventListener attributes
 * to allow multiple events to be observed.
 * </p>
 * 定义的接口允许Subject对象有不止一种状态可观察。如果主体直接实现了这个接口，那么它将只允许一种状态变化被观察到。
 * 如果受试者有多个状态变化需要观察，
 * 它可以定义多个EventListener属性来允许观察多个事件。
 *
 * <p>Such Listeners are used for many simulation entities such as {@link Vm} and {@link Cloudlet}.
 * Check the documentation of such interfaces that provides some Listeners.
 * </p>这些监听器用于许多模拟实体，如{@link Vm}和{@link Cloudlet}。检查提供一些监听器的接口的文档。
 *
 * @param <T> The class of the object containing information to be given to the
 * listener when the expected event happens.
 *  对象的类，其中包含在预期事件发生时要提供给侦听器的信息。
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */
@FunctionalInterface
public interface EventListener<T extends EventInfo> {

    /**
     * A implementation of Null Object pattern that makes nothing (it doesn't
     * perform any operation on each existing method). The pattern is used to
     * avoid NullPointerException's and checking everywhere if a listener object
     * is not null in order to call its methods.
     * 空对象模式的实现，什么都不做(它不对每个现有方法执行任何操作)。
     * 该模式用于避免NullPointerException，并检查所有侦听器对象是否为空，以便调用其方法。
     */
    EventListener NULL = (EventListener<? extends EventInfo>) (EventInfo info) -> {
    };

    /**
     * Gets notified when the observed object (also called subject of
     * observation) has changed. This method has to be called by the observed
     * objects to notify its state change to the listener.
     * 当被观察的对象(也称为观察的主体)发生变化时得到通知。被观察对象必须调用此方法，以将其状态更改通知给侦听器。
     *
     * @param info The data about the happened event.
     */
    void update(T info);
}

package com.hyunki.aryoulearning2.util

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T : ViewBinding>(
        val fragment: Fragment,
        val viewBindingFactory: (View) -> T
//implements the delegate interface, takeing fragment and viewbinding as types
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    fun init() {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            //sets binding to null on destroy
                            binding = null
                        }
                    })
                }
            }
        })
    }




    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        init()
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        //returns the constructor arg lambda,
        // which is the val that takes the root view of fragment,
        // and sets the binding to the variables
        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

//convenience ext function
fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
        ViewBindingDelegate(this, viewBindingFactory)

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T) =
        lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater)
        }
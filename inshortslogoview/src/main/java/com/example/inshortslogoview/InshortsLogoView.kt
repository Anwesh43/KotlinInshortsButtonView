package com.example.inshortslogoview

/**
 * Created by anweshmishra on 28/05/18.
 */

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

class InshortsLogoView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State (var prevScale : Float = 0f, var dir : Float = 0f, var scale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(updatecb : () -> Unit) {
            if (animated) {
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LinkedBlock (var cbs : ArrayList<(Canvas, Paint, Float) -> Unit>, val cb : (Canvas, Paint, Float) -> Unit = cbs[0]) {

        var next : LinkedBlock? = null

        var prev : LinkedBlock? = null

        private val state : State = State()

        init {
            cbs.removeAt(0)
            addNeighbor(cbs)
        }

        fun addNeighbor(cbs : ArrayList<(Canvas, Paint, Float) -> Unit>) {
            if (cbs.size > 0) {
                next = LinkedBlock(cbs)
                next?.prev = this
            }
        }

        fun getNext(dir: Int, cb : () -> Unit) : LinkedBlock {
            var curr : LinkedBlock? = this.prev
            if (dir == 1) {
                curr = this.next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            cb(canvas, paint, state.scale)
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }
}


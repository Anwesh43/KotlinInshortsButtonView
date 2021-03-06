package com.example.inshortslogoview

/**
 * Created by anweshmishra on 28/05/18.
 */

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

fun Canvas.getSize() : Float {
    val size = Math.min(width, height).toFloat()
    return size
}

fun Canvas.getCenter() : PointF {
    return PointF(width.toFloat() * 0.5f, height * 0.5f)
}

fun Canvas.drawInCenter(cb : () -> Unit) {
    save()
    translate(getCenter().x, getCenter().y)
    cb()
    restore()
}

class InshortsLogoView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
                updatecb()
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
            prev?.draw(canvas, paint)
            cb(canvas, paint, state.scale)
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }

    data class InshortsLogo (var i : Int) {

        var curr : LinkedBlock? = null

        var dir : Int = 1

        init {
            val cbs : ArrayList<(Canvas, Paint, Float) -> Unit> = ArrayList()
            cbs.add {canvas, paint, fl ->
                paint.color = Color.parseColor("#f44336")
                val size : Float = canvas.getSize() * fl
                canvas.drawRoundRect(RectF(-size/3, -size/3, size/3, size/3), size/10, size/10, paint)
            }
            cbs.add {canvas, paint, fl ->
                paint.color = Color.WHITE
                val gap : Float = (2 * canvas.getSize()/21)
                val x : Float = -canvas.getSize()/3 + 1.5f * gap
                val y : Float = -canvas.getSize()/3 + 1.5f * gap
                val r : Float = (gap * fl)/2
                canvas.drawRoundRect(RectF(x - r  , y - r, x+ 5 * r, y + r), r, r, paint)
            }

            fun addGridCircle(i : Int) {
                cbs.add {canvas, paint, fl ->
                    paint.color = Color.WHITE
                    val gap : Float = (2 * canvas.getSize()/21)
                    val x : Float = -canvas.getSize()/3 + 1.5f * gap
                    val y : Float = -canvas.getSize()/3 + 1.5f * gap
                    val r : Float = (gap)/2
                    canvas.drawCircle(x + 2 * gap * (i%3), y + 2 * gap * ((i/3)), r * fl, paint)
                }
            }

            for (i in 1..3) {
                for (j in 1..i) {
                    addGridCircle(((i - 1) * 3) + (3 - j))
                }
            }
            curr = LinkedBlock(cbs)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawInCenter {
                curr?.draw(canvas, paint)
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            curr?.update {
                curr = curr?.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr?.startUpdating(startcb)
        }
    }

    data class Renderer(var view : InshortsLogoView) {

        private val animator : Animator = Animator(view)

        private val inShortsLogo : InshortsLogo = InshortsLogo(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            inShortsLogo.draw(canvas, paint)
            animator.animate {
                inShortsLogo.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            inShortsLogo.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : InshortsLogoView {
            val view : InshortsLogoView = InshortsLogoView(activity)
            activity.setContentView(view)
            return view
        }
    }
}


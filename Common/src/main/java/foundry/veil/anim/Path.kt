package foundry.veil.anim

class Path(var frames: MutableList<Frame>, private var loop: Boolean, var pingPong: Boolean = false) {
    private var currentFrame: Frame

    init {
        populateFrames()
        currentFrame = frames[0]
    }

    private fun populateFrames(){
        val newFrames: MutableList<Frame> = mutableListOf()
        for(i in 0 .. frames.size){
            val frame = frames[i]
            newFrames.add(frame)
            if(frame is KeyFrame){
                for(j in 0 .. frame.duration){
                    val frameIndex = if(i+1 >= frames.size) if(loop) 0 else i else i+1
                    newFrames.add(frame.interpolate(frames[frameIndex], j/frame.duration.toFloat(), frame.easing))
                }
            } else {
                newFrames.add(frame)
            }
        }
        frames = newFrames
    }

    fun duration(): Int {
        var duration = 0
        for(frame in frames){
            if(frame is KeyFrame){
                duration += frame.duration
            }
        }
        return duration
    }

    fun reverse(){
        frames.reverse()
    }

    fun next() {
        currentFrame = (frames.indexOf(currentFrame)+1 % frames.size).let {
            frames[it]
        }
    }

    fun previous() {
        currentFrame = (frames.indexOf(currentFrame)-1 % frames.size).let {
            frames[it]
        }
    }

    fun reset() {
        currentFrame = frames[0]
    }

    fun tick() {
        if (currentFrame is KeyFrame) {
            if ((currentFrame as KeyFrame).duration > 0) {
                (currentFrame as KeyFrame).duration--
            } else {
                next()
            }
        } else {
            next()
        }
        println(currentFrame.pos)
    }

    /*
     * Returns the frame at the given progress
     * Use ticks from the block, not frames. Lerp between frames in the renderer
     */
    fun frameAtProgress(progress: Float): Frame {
        return frames[(frames.size * progress).toInt()]
    }

    fun getCurrentFrame(): Frame {
        return currentFrame
    }


}
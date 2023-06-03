package foundry.veil.anim

class Path(var frames: MutableList<Frame>, var duration: Int, var loop: Boolean, var pingPong: Boolean = false) {
    private var currentFrame: Frame = frames[0]

    init {
        populateFrames()
    }

    private fun populateFrames(){
        val newFrames: MutableList<Frame> = mutableListOf()
        for(i in 0 .. frames.size){
            val frame = frames[i]
            newFrames.add(frame)
            if(frame is KeyFrame){
                for(j in 0 .. frame.duration){
                    newFrames.add(frame.interpolate(frames[i+1], j/frame.duration.toFloat()))
                }
            } else {
                newFrames.add(frame)
            }
        }
        frames = newFrames
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
    }


}
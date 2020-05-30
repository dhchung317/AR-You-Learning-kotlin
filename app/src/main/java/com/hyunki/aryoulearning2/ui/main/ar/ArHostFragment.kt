package com.hyunki.aryoulearning2.ui.main.ar

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.airbnb.lottie.LottieAnimationView
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.animation.Animations
import com.hyunki.aryoulearning2.animation.LottieHelper
import com.hyunki.aryoulearning2.ui.main.ar.controller.GameCommandListener
import com.hyunki.aryoulearning2.ui.main.ar.controller.GameManager
import com.hyunki.aryoulearning2.ui.main.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

//TODO-figure out bounce/float animation
class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment(), GameCommandListener {

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    @Inject
    lateinit var lottieHelper: LottieHelper

    private lateinit var progressBar: ProgressBar

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArGameFragment
    private lateinit var gameManager: GameManager
    private lateinit var listener: NavListener

    private lateinit var playBalloonPop: MediaPlayer

    private lateinit var frameLayout: FrameLayout

    private lateinit var wordContainer: LinearLayout
    private lateinit var wordValidatorLayout: View
    private lateinit var wordCardView: CardView
    private lateinit var wordValidatorCv: CardView
    private lateinit var wordValidator: TextView
    private lateinit var validatorWord: TextView
    private lateinit var validatorWrongWord: TextView
    private lateinit var validatorWrongPrompt: TextView
    private lateinit var validatorImage: ImageView
    private lateinit var validatorBackgroundImage: ImageView
    private lateinit var validatorOkButton: Button
    private lateinit var undo: ImageButton

    private lateinit var exitMenu: View
    private lateinit var exit: ImageButton
    private lateinit var exitYes: Button
    private lateinit var exitNo: Button

    private lateinit var tapAnimation: LottieAnimationView
    private lateinit var fadeIn: ObjectAnimator
    private lateinit var fadeOut: ObjectAnimator

    private lateinit var gestureDetector: GestureDetector
    private lateinit var base: Node
    private var mainAnchor: Anchor? = null
    private var mainAnchorNode: AnchorNode? = null
    private lateinit var mainHit: HitResult


    private var hasFinishedLoadingModels = false
    private var hasFinishedLoadingLetters = false
    private var hasPlacedGame = false
    private var placedAnimation = false

    private var modelMapList: List<MutableMap<String, ModelRenderable>> = ArrayList()
    private var letterMap = mutableMapOf<String, ModelRenderable>()

//    private val textToSpeech: TextToSpeech

//    init {
//        this.textToSpeech = pronunciationUtil.textToSpeech
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressBar = activity!!.findViewById(R.id.progress_bar)
    }

    override fun onAttach(context: Context) {
        (activity!!.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }

        //        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_arfragment_host, container, false)
        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment).let { it as ArGameFragment }
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //        playBalloonPop = MediaPlayer.create(getContext(), R.raw.pop_effect);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frameLayout = view.findViewById(R.id.frame_layout)

        setUpViews(view)

        gestureDetector = getGestureDetector()

        setUpARScene(arFragment)

        requestCameraPermission(activity, RC_PERMISSIONS)

        arViewModel = ViewModelProviders.of(this, viewModelProviderFactory).get(ArViewModel::class.java)

        runViewModel(arViewModel)
    }

    private fun runViewModel(arViewModel: ArViewModel){

        arViewModel.getModelLiveData().observe(viewLifecycleOwner,
                Observer { models -> processModelData(models) })

        arViewModel.getFutureModelMapListLiveData().observe(viewLifecycleOwner,
                Observer { hashMapList ->

                    processFutureModelMapList(hashMapList)

                    arViewModel.loadModelRenderables(hashMaps)
                    arViewModel.loadMapOfFutureLetters(hashMaps)
                })

        arViewModel.futureLetterMapLiveData.observe(viewLifecycleOwner,
                Observer { map ->
                    arViewModel.loadLetterRenderables(map) })

        arViewModel.modelMapList.observe(viewLifecycleOwner,
                Observer { hashMaps ->
                    modelMapList = hashMaps
                    hasFinishedLoadingModels = true
                })

        arViewModel.letterMap.observe(viewLifecycleOwner, Observer { returnMap ->
            letterMap = returnMap
            hasFinishedLoadingLetters = true
        })

        arViewModel.fetchModelsFromRepository()
    }

    private fun processModelData(state: ArState){
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> showProgressBar(false)
            is ArState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                arViewModel.loadListMapsOfFutureModels(state.responses)
            }
        }
    }

    private fun processFutureModelMapList(state: ArState){
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> showProgressBar(false)
            is ArState.Success.OnFutureModelMapListLoaded -> {
                showProgressBar(false)
                arViewModel.loadMapOfFutureLetters(state.futureModelMapList)
                arViewModel.loadModelRenderables(state.futureModelMapList)
            }
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun getKeysFromModelMapList(mapList: List<MutableMap<String, ModelRenderable>>): List<String> {
        val keys = ArrayList<String>()
        for (i in mapList.indices) {
            for ((key) in mapList[i]) {
                keys.add(key)
            }
        }
        return keys
    }

    private fun setUpViews(view: View) {
        initViews(view)
        setListeners()
        //        setAnimations();
    }

    private fun getGestureDetector(): GestureDetector {
        return GestureDetector(
                activity,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        onSingleTap(e)
                        return true
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }
                })
    }

    private fun setUpARScene(arFragment: ArFragment) {
        setOnTouchListener(arFragment)
        setAddOnUpdateListener(arFragment)
    }

    private fun setOnTouchListener(arFragment: ArFragment) {
        val scene = arFragment.arSceneView.scene
        scene.setOnTouchListener { _: HitTestResult, event: MotionEvent ->
            if (!hasPlacedGame) {
                gestureDetector.onTouchEvent(event)
            }
            false
        }
    }

    private fun setAddOnUpdateListener(arFragment: ArFragment) {
        val scene = arFragment.arSceneView.scene
        scene.addOnUpdateListener { _ ->
            val frame = arFragment.arSceneView.arFrame

            if (frame == null) {
                return@addOnUpdateListener
            }

            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return@addOnUpdateListener
            }

            if (!hasPlacedGame) {
                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (!placedAnimation && plane.trackingState == TrackingState.TRACKING) {
                        placedAnimation = true
                        tapAnimation = lottieHelper.getAnimationView(context, LottieHelper.AnimationType.TAP)
                        activity?.let { lottieHelper.addTapAnimationToScreen(tapAnimation, it, frameLayout) }
                    }
                }
            }
        }
    }

    private fun initViews(view: View) {
        wordCardView = view.findViewById(R.id.card_wordContainer)
        wordContainer = view.findViewById(R.id.word_container)
        wordValidatorLayout = layoutInflater.inflate(R.layout.validator_card, frameLayout, false)
        //        wordValidatorCv = wordValidatorLayout.findViewById(R.id.word_validator_cv);
        wordValidator = wordValidatorLayout.findViewById(R.id.word_validator)
        validatorImage = wordValidatorLayout.findViewById(R.id.validator_imageView)
        validatorBackgroundImage = wordValidatorLayout.findViewById(R.id.correct_star_imageView)
        validatorWord = wordValidatorLayout.findViewById(R.id.validator_word)
        validatorWrongPrompt = wordValidatorLayout.findViewById(R.id.validator_incorrect_prompt)
        validatorWrongWord = wordValidatorLayout.findViewById(R.id.validator_wrong_word)
        validatorOkButton = wordValidatorLayout.findViewById(R.id.button_validator_ok)
        //        wordValidatorCv.setVisibility(View.INVISIBLE);
        exitMenu = layoutInflater.inflate(R.layout.exit_menu_card, frameLayout, false)
        exit = view.findViewById(R.id.exit_imageButton)
        exitYes = exitMenu.findViewById(R.id.exit_button_yes)
        exitNo = exitMenu.findViewById(R.id.exit_button_no)
        undo = view.findViewById(R.id.button_undo)
    }

    private fun setListeners() {
        exit.setOnClickListener { frameLayout.addView(exitMenu) }
        exitYes.setOnClickListener { listener.moveToListFragment() }
        exitNo.setOnClickListener { frameLayout.removeView(exitMenu) }
        undo.setOnClickListener { undoLastLetter() }
    }

    //TODO - refactor animations to separate class
    //
    private fun setAnimations() {
        fadeIn = Animations.Normal().setCardFadeInAnimator(wordValidatorCv)

        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                frameLayout.addView(wordValidatorLayout)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                validatorOkButton.setOnClickListener { v ->
                    fadeOut.startDelay = 500
                    fadeOut.start()
                }
            }
        })

        fadeOut = Animations.Normal().setCardFadeOutAnimator(wordValidatorCv)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                frameLayout.removeView(wordValidatorLayout)

                //                if (roundCounter < roundLimit && roundCounter < modelMapList.size()) {
                //                    createNextGame(modelMapList.get(roundCounter));
                //                } else {
                //                    moveToReplayFragment();
                //                }
            }
        })
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!hasFinishedLoadingModels || !hasFinishedLoadingLetters) {
            // We can't do anything yet.
            return
        }

        val frame = arFragment.arSceneView.arFrame
        if (frame != null) {
            if (!hasPlacedGame && tryPlaceGame(tap, frame)) {
                hasPlacedGame = true
                frameLayout.removeView(tapAnimation)
            }
        }
    }

    private fun tryPlaceGame(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {

            mainHit = frame.hitTest(tap)[0]

            val trackable = mainHit.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(mainHit.hitPose)) {
                // Create the Anchor.
                if (trackable.getTrackingState() == TrackingState.TRACKING) {
                    mainAnchor = mainHit.createAnchor()
                }

                mainAnchorNode = AnchorNode(mainAnchor)
                mainAnchorNode!!.setParent(arFragment.arSceneView.scene)
                //                    Node gameSystem = createGame(modelMapList.get(0));
                gameManager = GameManager(getKeysFromModelMapList(arViewModel.modelMapList.value!!), this, listener)
                Log.d("arhostfrag", "tryPlaceGame: " + arViewModel.modelMapList.value!!.size)
                val modelKey = gameManager.currentWordAnswer

                wordCardView.visibility = View.VISIBLE

                for (i in modelMapList.indices) {
                    for ((key, value) in modelMapList[i]) {

                        if (key == modelKey) {
                            createSingleGame(value, key)
                        }

                    }
                }
                return true
            }
        }
        return false
    }


    private fun createSingleGame(mainModel: ModelRenderable, name: String) {
        base = ModelUtil.getGameAnchor(mainModel)
        mainAnchorNode?.addChild(base)
        placeLetters(name)
    }

    private fun refreshModelResources() {
        mainAnchorNode?.anchor?.detach()
        mainAnchor = null
        mainAnchorNode = null
    }

    private fun addLetterToWordContainer(letter: String) {
        val ballonTF = ResourcesCompat.getFont(activity!!, R.font.balloon)
        val t = TextView(activity)
        t.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        t.typeface = ballonTF
        t.setTextColor(resources.getColor(R.color.colorWhite))
        t.textSize = 100f
        t.text = letter
        t.textAlignment = View.TEXT_ALIGNMENT_CENTER
        wordContainer.addView(t)
    }

    override fun onDestroy() {
        super.onDestroy()
//        textToSpeech.shutdown()
        pronunciationUtil = null
        //        playBalloonPop.reset();
        //        playBalloonPop.release();
    }

    private fun placeLetters(word: String) {
        for (letter in word) {
            placeSingleLetter(
                    letter.toString())
        }
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode = ModelUtil.getLetter(base, letterMap[letter], arFragment!!)
        letterAnchorNode.children[0].setOnTapListener(getNodeOnTapListener(letter, letterAnchorNode))

        Log.d("arx", "tryPlaceGame: " + letterMap[letter]!!)
        connectAnchorToBase(letterAnchorNode)
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment.arSceneView.scene.addChild(anchorNode)
        base.addChild(anchorNode)
    }

    private fun recreateErasedLetter(letterToRecreate: String) {
        if (letterToRecreate != "") {
            placeSingleLetter(letterToRecreate)
        }
    }

    private fun getNodeOnTapListener(letterString: String, letterAnchorNode: AnchorNode): Node.OnTapListener {

        return Node.OnTapListener { hitTestResult, motionEvent ->
            run {
                gameManager.addLetterToAttempt(letterString)

                lottieHelper.addAnimationViewOnTopOfLetter(
                        getLetterTapAnimation(
                                checkIfTappedLetterIsCorrect(letterString)),
                        (motionEvent.x - 7).roundToInt(),
                        (motionEvent.y + 7).roundToInt(),
                        frameLayout)

                addLetterToWordBox(letterString.toLowerCase())
                gameManager.addTappedLetterToCurrentWordAttempt(letterString.toLowerCase())
                Objects.requireNonNull<Anchor>(letterAnchorNode.anchor).detach()
            }
        }
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter =
                gameManager.currentWordAnswer[gameManager.attempt.length - 1].toString()

        return tappedLetter.toLowerCase() == correctLetter.toLowerCase()
    }

    private fun getLetterTapAnimation(isCorrect: Boolean): LottieAnimationView {
        return if (isCorrect) {
            lottieHelper.getAnimationView(
                    activity, LottieHelper.AnimationType.SPARKLES)
        } else {
            lottieHelper.getAnimationView(
                    activity, LottieHelper.AnimationType.ERROR)
        }
    }

    override fun startNextGame(modelKey: String) {
        Log.d("startnextgame:arhostfragment", "startNextGame: condition hit")
        refreshModelResources()

        mainAnchor = mainHit.createAnchor()
        mainAnchorNode = AnchorNode(mainAnchor)
        mainAnchorNode!!.setParent(arFragment.arSceneView.scene)

        for (i in modelMapList.indices) {
            for ((key, value) in modelMapList[i]) {
                if (key == modelKey) {
                    createSingleGame(value, key)
                }
            }
        }
        wordContainer.removeAllViews()
    }

    fun addLetterToWordBox(letter: String) {
        addLetterToWordContainer(letter)
    }

    fun undoLastLetter() {
        val erasedLetter = gameManager.subtractLetterFromAttempt()
        recreateErasedLetter(erasedLetter)
        if (wordContainer.childCount > 0) {
            wordContainer.removeViewAt(wordContainer.childCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasPlacedGame = false
        placedAnimation = false
    }

    companion object {
        private const val RC_PERMISSIONS = 0x123
        fun requestCameraPermission(activity: Activity?, requestCode: Int) {
            activity?.let {
                ActivityCompat.requestPermissions(
                        it, arrayOf(Manifest.permission.CAMERA), requestCode)
            }
        }
    }
}
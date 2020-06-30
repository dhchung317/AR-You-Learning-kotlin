package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameCommandListener
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameManager
import com.hyunki.aryoulearning2.ui.main.fragment.ar.customview.ValidatorCardView
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ViewUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

//TODO set validator views with appropriate text and image
class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment(), GameCommandListener {
    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    @Inject
    lateinit var lottieHelper: LottieHelper

    @Inject
    lateinit var application: Application

    private lateinit var modelUtil: ModelUtil
    private lateinit var modelList: List<Model>

    private lateinit var progressBar: ProgressBar

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArGameFragment
    private lateinit var gameManager: GameManager
    private lateinit var listener: NavListener

    private lateinit var playBalloonPop: MediaPlayer

    private lateinit var frameLayout: FrameLayout

    private lateinit var wordContainer: LinearLayout
    private lateinit var undo: ImageButton
    private lateinit var wordContainerCardView: CardView

    private lateinit var validatorCardView: ValidatorCardView

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

    private lateinit var category: String

    private var hasPlacedGame = false
    private var hasPlacedAnimation = false

    private var modelMapList: List<MutableMap<String, ModelRenderable>> = ArrayList()
    private var letterMap = mapOf<String, ModelRenderable>()

    val balloonTF by lazy { ResourcesCompat.getFont(requireActivity(), R.font.balloon) }

    //TODO implement text to speech
//    private val textToSpeech: TextToSpeech
//    init {
//        this.textToSpeech = pronunciationUtil.textToSpeech
//    }

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
        //        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    //TODO implement audio effects
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpResultListener()
        //        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //        playBalloonPop = MediaPlayer.create(getContext(), R.raw.pop_effect);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_arhost, container, false)
        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment).let { it as ArGameFragment }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!checkPermission()) {
            requestCameraPermission(requireActivity(), RC_PERMISSIONS)
        }

        progressBar = requireActivity().findViewById(R.id.progress_bar)
        frameLayout = view.findViewById(R.id.frame_layout)
        setUpViews(view)
        gestureDetector = getGestureDetector()
        arViewModel = ViewModelProvider(this, viewModelProviderFactory).get(ArViewModel::class.java)
        setUpARScene(arFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasPlacedGame = false
        hasPlacedAnimation = false
    }

    //TODO implement audio effects shutdown
    override fun onDestroy() {
        super.onDestroy()

//        textToSpeech.shutdown()
        pronunciationUtil = null
        //        playBalloonPop.reset();
        //        playBalloonPop.release();

    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun startNextGame(modelKey: String) {
        refreshModelResources()
        if (mainHit.trackable.trackingState == TrackingState.TRACKING) {
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
        } else {
            hasPlacedGame = false
            hasPlacedAnimation = false
            gestureDetector = getGestureDetector()
            setUpARScene(arFragment)
        }
        wordContainer.removeAllViews()
    }

    override fun showCard(isCorrect: Boolean) {
        validatorCardView.answerText = gameManager.currentWord.answer
        validatorCardView.answerImage = gameManager.currentWord.image

        when (isCorrect) {
            true -> setUpCardWithCorrectValidators()
            else -> setUpCardWithIncorrectValidators()
        }

        validatorCardView.okButton.setOnClickListener {
            onHidingCard(isCorrect)
            fadeOut.startDelay = 500
            fadeOut.start()
        }
        fadeIn.start()
    }

    override fun onHidingCard(wasCorrect: Boolean) {
        gameManager.onHidingCard(wasCorrect)
    }

    private fun setUpCardWithCorrectValidators() {
        validatorCardView.headerText = "GREAT!!!"
        validatorCardView.backgroundImage = R.drawable.star
        validatorCardView.wrongAnswerVisibility = View.INVISIBLE
        validatorCardView.wrongAnswerPromptVisibility = View.INVISIBLE
    }

    private fun setUpCardWithIncorrectValidators() {
        validatorCardView.headerText = "TRY AGAIN..."
        validatorCardView.backgroundImage = R.drawable.error
        validatorCardView.wrongAnswerVisibility = View.VISIBLE
        validatorCardView.wrongAnswerPromptVisibility = View.VISIBLE
        validatorCardView.wrongAnswerText = gameManager.attempt
    }

    private fun setUpViews(view: View) {
        initViews(view)
        setListeners()
        setAnimations();
    }

    private fun initViews(view: View) {
        validatorCardView = view.findViewById(R.id.validator_card)
        wordContainerCardView = view.findViewById(R.id.word_container_card)
        wordContainer = view.findViewById(R.id.word_container)

        exitMenu = layoutInflater.inflate(R.layout.exit_menu_card, frameLayout, false)
        exit = view.findViewById(R.id.exit_imageButton)
        exitYes = exitMenu.findViewById(R.id.exit_button_yes)
        exitNo = exitMenu.findViewById(R.id.exit_button_no)

        undo = view.findViewById(R.id.button_undo)
        validatorCardView.visibility = View.INVISIBLE;
    }

    private fun setListeners() {
        exit.setOnClickListener { frameLayout.addView(exitMenu) }
        exitYes.setOnClickListener { listener.moveToListFragment() }
        exitNo.setOnClickListener { frameLayout.removeView(exitMenu) }
        undo.setOnClickListener { undoLastLetter() }
    }

    private fun getGestureDetector(): GestureDetector {
        return GestureDetector(
                requireActivity(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        onSingleTap(e)
                        return true
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        onSingleTap(e)
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
            val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener

            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return@addOnUpdateListener
            }
            if (!hasPlacedGame) {
                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (!hasPlacedAnimation && plane.trackingState == TrackingState.TRACKING) {
                        placeAnimation()
                    }
                }
            }
        }
    }

    private fun placeAnimation() {
        hasPlacedAnimation = true
        tapAnimation = lottieHelper.getAnimationView(application, LottieHelper.AnimationType.TAP)
        tapAnimation = lottieHelper.placeTapAnimationOnScreen(
                tapAnimation,
                requireActivity().window.decorView.width,
                requireActivity().window.decorView.height)
        frameLayout.addView(tapAnimation, 500, 500)
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!arViewModel.isLettersLoaded() || !arViewModel.isModelsLoaded()) {
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
                if (!this::gameManager.isInitialized) {
                    gameManager = GameManager(modelList, this, listener)
                    modelUtil = gameManager.modelUtil
                }
                if (trackable.getTrackingState() == TrackingState.TRACKING) {
                    mainAnchor = mainHit.createAnchor()
                }
                mainAnchorNode = AnchorNode(mainAnchor)
                mainAnchorNode!!.setParent(arFragment.arSceneView.scene)
                val modelKey = gameManager.getCurrentWordAnswer()
                wordContainerCardView.visibility = View.VISIBLE

                modelMapList.stream()
                        .filter { it.containsKey(modelKey) }
                        .findFirst()
                        .apply {
                            this.ifPresent {
                                it[modelKey]?.let { model -> createSingleGame(model, modelKey) }
                            }
                        }
                return true
            }
        }
        return false
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    @ExperimentalCoroutinesApi
    private fun processModelData(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                modelList = state.models
                arViewModel.getListOfMapsOfFutureModels(state.models).observe(viewLifecycleOwner, Observer {
                    processFutureModelMapList(it)
                })
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun processFutureModelMapList(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnFutureModelMapListLoaded -> {
                showProgressBar(false)
                arViewModel.getMapOfFutureLetters(state.futureModelMapList).observe(viewLifecycleOwner, Observer {
                    processFutureLetterMap(it)
                })
                arViewModel.getModelRenderables(state.futureModelMapList).observe(viewLifecycleOwner, Observer {
                    processModelMapList(it)
                })
            }
        }
    }

    private fun processFutureLetterMap(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnFutureLetterMapLoaded -> {
                showProgressBar(false)
                arViewModel.getLetterRenderables(state.futureLetterMap).observe(viewLifecycleOwner, Observer {
                    processLetterMap(it)
                })
            }
        }
    }

    private fun processModelMapList(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnModelMapListLoaded -> {
                showProgressBar(false)
                modelMapList = state.modelMap
            }
        }
    }

    private fun processLetterMap(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnLetterMapLoaded -> {
                showProgressBar(false)
                letterMap = state.letterMap
            }
        }
    }

    //TODO - refactor animations to separate class
    private fun setAnimations() {
        fadeIn = Animations.Normal().setCardFadeInAnimator(validatorCardView)

        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                validatorCardView.visibility = View.VISIBLE
                exit.isClickable = false
                validatorCardView.okButton.isClickable = false
            }
            override fun onAnimationEnd(animation: Animator?) {
                validatorCardView.bringToFront()
                validatorCardView.okButton.isClickable = true
            }
        })
        fadeOut = Animations.Normal().setCardFadeOutAnimator(validatorCardView)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                validatorCardView.visibility = View.INVISIBLE
                exit.isClickable = true
            }
        })
    }

    private fun createSingleGame(mainModel: ModelRenderable, name: String) {
        base = modelUtil.getGameAnchor(mainModel)
        mainAnchorNode?.addChild(base)
        placeLetters(name)
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment.arSceneView.scene.addChild(anchorNode)
        base.addChild(anchorNode)
    }

    private fun addLetterToWordContainer(letter: String) {
        val t =
                ViewUtil.configureWordContainerTextView(
                        TextView(activity), letter, balloonTF, ContextCompat.getColor(requireContext(), R.color.colorWhite))
        wordContainer.addView(t)
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode = modelUtil.getLetter(base, letterMap[letter], arFragment)
        letterAnchorNode.children[0].setOnTapListener(getNodeOnTapListener(letter, letterAnchorNode))
        connectAnchorToBase(letterAnchorNode)
    }

    private fun placeLetters(word: String) {
        for (letter in word) {
            placeSingleLetter(
                    letter.toString())
        }
    }

    private fun addLetterToWordBox(letter: String) {
        addLetterToWordContainer(letter)
    }

    private fun getLetterTapAnimation(isCorrect: Boolean): LottieAnimationView {
        return when (isCorrect) {
            true -> {
                lottieHelper.getAnimationView(activity,
                        LottieHelper.AnimationType.SPARKLES)
            }
            else -> {
                lottieHelper.getAnimationView(activity,
                        LottieHelper.AnimationType.ERROR)
            }
        }
    }

    private fun undoLastLetter() {
        val erasedLetter = gameManager.subtractLetterFromAttempt()
        recreateErasedLetter(erasedLetter)
        if (wordContainer.childCount > 0) {
            wordContainer.removeViewAt(wordContainer.childCount - 1)
        }
    }

    private fun recreateErasedLetter(letterToRecreate: String) {
        if (letterToRecreate != "") {
            placeSingleLetter(letterToRecreate)
        }
    }

    private fun getNodeOnTapListener(letterString: String, letterAnchorNode: AnchorNode): Node.OnTapListener {
        return Node.OnTapListener { _, motionEvent ->
            addLetterToWordBox(letterString)
            letterAnchorNode.anchor?.detach()
            val isCorrect = gameManager.addTappedLetterToCurrentWordAttempt(letterString)
            val lav =
                    lottieHelper.getAnimationViewOnTopOfLetter(
                            getLetterTapAnimation(isCorrect),
                            (motionEvent.x - 7).roundToInt(),
                            (motionEvent.y + 7).roundToInt())

            frameLayout.addView(lav, 300, 300)
            lav.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    lav.elevation = 0f
                }
                override fun onAnimationEnd(animation: Animator?) {
//                    frameLayout.removeViewInLayout(lav)
                    frameLayout.removeView(lav)
                }
            })
            gameManager.onWordAnswered()
        }
    }

    private fun refreshModelResources() {
        mainAnchorNode?.anchor?.detach()
        mainAnchor = null
        mainAnchorNode = null
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
                REQUEST_KEY,
                this,
                FragmentResultListener { requestKey, result ->
                    onFragmentResult(requestKey, result)
                })
    }

    @ExperimentalCoroutinesApi
    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (REQUEST_KEY == requestKey) {
            category = result.getString(KEY_ID)
            arViewModel.getModelsFromRepositoryByCategory(category)
                    .observe(viewLifecycleOwner, Observer {
                        processModelData(it)
                    })
        }
    }

    companion object {
        private const val TAG = "arhostfragment"
        private const val RC_PERMISSIONS = 0x123
        fun requestCameraPermission(activity: Activity?, requestCode: Int) {
            activity?.let {
                ActivityCompat.requestPermissions(
                        it, arrayOf(Manifest.permission.CAMERA), requestCode)
            }
        }

        const val REQUEST_KEY = "get-current-category"
        const val KEY_ID = "current-category"
    }
}
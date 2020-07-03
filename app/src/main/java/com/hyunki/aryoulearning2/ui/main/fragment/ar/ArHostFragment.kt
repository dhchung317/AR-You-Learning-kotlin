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
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
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
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.databinding.ExitMenuCardBinding
import com.hyunki.aryoulearning2.databinding.FragmentArhostBinding
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameCommandListener
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameManager
import com.hyunki.aryoulearning2.ui.main.fragment.ar.customview.ValidatorCardView
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ArModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ViewUtil
import com.hyunki.aryoulearning2.ui.main.fragment.category.CategoryFragment
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.hint.HintFragment
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.util.viewBinding
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

//TODO set validator views with appropriate text and image
@ExperimentalCoroutinesApi
class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment(), GameCommandListener {
    val binding by viewBinding(FragmentArhostBinding::bind)
//    val exitBinding by viewBinding(ExitMenuCardBinding::bind)

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    @Inject
    lateinit var lottieHelper: LottieHelper

    @Inject
    lateinit var application: Application

    private lateinit var progressBar: ProgressBar

    private lateinit var arViewModel: ArViewModel
    private var arFragment: ArGameFragment? = null
    private var gameManager: GameManager? = null
    private var listener: NavListener? = null

    private lateinit var playBalloonPop: MediaPlayer

    private var frameLayout: FrameLayout? = null
    private var wordContainer: LinearLayout? = null
//    private lateinit var wordContainerCardView: CardView
//    private lateinit var wordContainerUndoButton: ImageButton

    private var validatorCardView: ValidatorCardView? = null

    private var exitMenuDialog: View? = null
    private var exitButton: ImageButton? = null
    private var exitYes: Button? = null
    private var exitNo: Button? = null

    private var tapAnimation: LottieAnimationView? = null
    private var fadeIn: ObjectAnimator? = null
    private var fadeOut: ObjectAnimator? = null

    private var gestureDetector: GestureDetector? = null

    private var base: Node? = null
    private var mainAnchor: Anchor? = null
    private var mainAnchorNode: AnchorNode? = null
    private var mainHit: HitResult? = null

    private lateinit var category: String

    private var hasPlacedGame = false
    private var hasPlacedAnimation = false

    private lateinit var arModelUtil: ArModelUtil
    private lateinit var arModelList: List<ArModel>
    private var modelMapList: List<MutableMap<String, ModelRenderable>> = ArrayList()
    private var letterMap = mapOf<String, ModelRenderable>()

    private val balloonTF by lazy { ResourcesCompat.getFont(requireActivity().applicationContext, R.font.balloon) }

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
//        progressBar = requireActivity().findViewById(R.id.progress_bar)
//        frameLayout = view.findViewById(R.id.frame_layout)
        setUpViews(view)
        gestureDetector = getGestureDetector()
        arViewModel = ViewModelProvider(this, viewModelProviderFactory).get(ArViewModel::class.java)
        setUpARScene(arFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onStop() {
        hasPlacedGame = false
        hasPlacedAnimation = false
        wordContainer = null
        validatorCardView = null
        validatorCardView?.clearAnimation()
        exitMenuDialog = null
        frameLayout = null
        gameManager = null
        arFragment = null
        gestureDetector = null
        exitButton = null
        exitYes = null
        exitNo = null

        pronunciationUtil = null
        listener = null
        base = null
        mainAnchor = null
        mainAnchorNode = null
        mainHit = null
        tapAnimation = null
        fadeIn?.removeAllListeners()
        fadeOut?.removeAllListeners()
        fadeIn = null
        fadeOut = null
        super.onStop()
    }

    //TODO implement audio effects shutdown
    override fun onDestroy() {

        super.onDestroy()

//        textToSpeech.shutdown()
        //        playBalloonPop.reset();
        //        playBalloonPop.release();

    }

    override fun onResume() {
        super.onResume()
        startViewModelProcesses(category)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    //TODO refactor the following two methods

    override fun startGame(modelKey: String) {
        //TODO examine if the statement checks correctly
        if (!gameManager!!.isFirstGame()) {
            refreshModelResources()
        }
        if (mainHit!!.trackable.trackingState == TrackingState.TRACKING) {
            mainAnchor = mainHit!!.createAnchor()
            mainAnchorNode = AnchorNode(mainAnchor)
            mainAnchorNode?.setParent(arFragment!!.arSceneView.scene)

            filterByKey(modelMapList, modelKey)?.apply {
                this.ifPresent {
                    it[modelKey]?.let { model -> createSingleGame(model, modelKey) }
                }
            }
        } else {
            restartPlaneSearch()
        }
        wordContainer?.removeAllViews()
    }

    private fun tryPlaceGame(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            mainHit = frame.hitTest(tap)[0]
            val trackable = mainHit!!.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(mainHit!!.hitPose)) {
                if (gameManager == null || gameManager?.isGameOverState()!!) {
                    assignNewGameManager(arModelList, this, listener)
                }
//                if (gameManager.isGameOverState()) {
//                    assignNewGameManager(arModelList, this, listener)
//                }
                binding.wordContainerCard.visibility = View.VISIBLE
//TODO examine follwoing method
                startGame(modelKey = gameManager!!.getCurrentWordAnswer())
                return true
            }
        }
        return false
    }

    private fun assignNewGameManager(list: List<ArModel>, gameCommands: GameCommandListener, listener: NavListener?) {
        gameManager = GameManager(list, gameCommands, listener)
        arModelUtil = gameManager!!.arModelUtil
    }

    private fun restartPlaneSearch() {
        hasPlacedGame = false
        hasPlacedAnimation = false
        gestureDetector = getGestureDetector()
        setUpARScene(arFragment)
    }

    override fun showCard(isCorrect: Boolean) {
        validatorCardView!!.answerText = gameManager!!.currentWord.answer
        validatorCardView!!.answerImage = gameManager!!.currentWord.image

        when (isCorrect) {
            true -> setUpCardWithCorrectValidators(validatorCardView!!)
            else -> setUpCardWithIncorrectValidators(validatorCardView!!)
        }

        validatorCardView!!.okButton.setOnClickListener {
            onHidingCard(isCorrect)
            fadeOut?.startDelay = 500
            fadeOut?.start()
        }
        fadeIn?.start()
    }

    override fun onHidingCard(wasCorrect: Boolean) {
        gameManager?.onHidingCard(wasCorrect)
    }

    private fun setUpCardWithCorrectValidators(v: ValidatorCardView) {
        v.headerText = "GREAT!!!"
        v.backgroundImage = R.drawable.star
        v.wrongAnswerVisibility = View.INVISIBLE
        v.wrongAnswerPromptVisibility = View.INVISIBLE
    }

    private fun setUpCardWithIncorrectValidators(v: ValidatorCardView) {
        v.headerText = "TRY AGAIN..."
        v.backgroundImage = R.drawable.error
        v.wrongAnswerVisibility = View.VISIBLE
        v.wrongAnswerPromptVisibility = View.VISIBLE
        v.wrongAnswerText = gameManager!!.attempt
    }

    private fun setUpViews(view: View) {
        initViews(view)
        setListeners()
        validatorCardView?.let { setAnimations(it) };
    }

    private fun initViews(view: View) {
        frameLayout = binding.frameLayout
        validatorCardView = binding.validatorCard
//        wordContainerCardView = view.findViewById(R.id.word_container_card)
        wordContainer = binding.wordContainer
        exitButton = binding.exitImageButton

        exitMenuDialog = layoutInflater.inflate(R.layout.exit_menu_card, frameLayout, false)
        exitYes = exitMenuDialog!!.findViewById(R.id.exit_button_yes)
        exitNo = exitMenuDialog!!.findViewById(R.id.exit_button_no)

//        wordContainerUndoButton = view.findViewById(R.id.button_undo)
        validatorCardView!!.visibility = View.INVISIBLE;
    }

    private fun setListeners() {
        exitButton?.setOnClickListener { frameLayout?.addView(exitMenuDialog) }
        exitYes?.setOnClickListener { listener?.moveToListFragment() }
        exitNo?.setOnClickListener { frameLayout?.removeView(exitMenuDialog) }
        binding.buttonUndo.setOnClickListener { undoLastLetter() }
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

    private fun setUpARScene(arFragment: ArFragment?) {
        setOnTouchListener(arFragment)
        setAddOnUpdateListener(arFragment)
    }

    private fun setOnTouchListener(arFragment: ArFragment?) {
        val scene = arFragment?.arSceneView?.scene
        scene?.setOnTouchListener { _: HitTestResult, event: MotionEvent ->
            if (!hasPlacedGame) {
                gestureDetector?.onTouchEvent(event)
            }
            false
        }
    }

    private fun setAddOnUpdateListener(arFragment: ArFragment?) {
        val scene = arFragment!!.arSceneView.scene
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
                tapAnimation!!,
                requireActivity().window.decorView.width,
                requireActivity().window.decorView.height)
        frameLayout?.addView(tapAnimation, 500, 500)
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!arViewModel.isLettersLoaded() || !arViewModel.isModelsLoaded()) {
            // We can't do anything yet.
            return
        }
        val frame = arFragment!!.arSceneView.arFrame

        if (frame != null) {
            if (!hasPlacedGame && tryPlaceGame(tap, frame)) {
                hasPlacedGame = true
                frameLayout?.removeView(tapAnimation)
            }
        }
    }

    private fun filterByKey(modelMapList: List<MutableMap<String, ModelRenderable>>, key: String): Optional<MutableMap<String, ModelRenderable>>? {
        return modelMapList.stream()
                .filter { it.containsKey(key) }
                .findFirst()
    }

    private fun showProgressBar(isVisible: Boolean) {
//        if (isVisible) {
//            progressBar.visibility = View.VISIBLE
//        } else {
//            progressBar.visibility = View.GONE
//        }
    }

    private fun processModelData(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Error -> {
                showProgressBar(false)
            }
            is ArState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                arModelList = state.arModels
                arViewModel.getListOfMapsOfFutureModels(state.arModels).observe(viewLifecycleOwner, Observer {
                    processFutureModelMapList(it)
                })
            }
        }
    }

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
    private fun setAnimations(v: ValidatorCardView) {
        fadeIn = Animations.Normal().setCardFadeInAnimator(v)

        fadeIn?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                v.visibility = View.VISIBLE
                exitButton?.isClickable = false
                v.okButton.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                v.bringToFront()
                v.okButton.isClickable = true
            }
        })
        fadeOut = Animations.Normal().setCardFadeOutAnimator(v)
        fadeOut?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                v.okButton.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator) {
                v.visibility = View.INVISIBLE
                exitButton?.isClickable = true
            }
        })
    }

    private fun createSingleGame(mainModel: ModelRenderable, name: String) {
        base = arModelUtil.getGameAnchor(mainModel)
        mainAnchorNode?.addChild(base)
        placeLetters(name)
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment!!.arSceneView.scene.addChild(anchorNode)
        base!!.addChild(anchorNode)
    }

    private fun addLetterToWordContainer(letter: String) {
        val t =
                ViewUtil.configureWordContainerTextView(
                        TextView(requireContext().applicationContext), letter, balloonTF, ContextCompat.getColor(requireContext(), R.color.colorWhite))
        wordContainer?.addView(t)
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode = arModelUtil.getLetter(base, letterMap[letter], arFragment)
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
                lottieHelper.getAnimationView(requireContext().applicationContext,
                        LottieHelper.AnimationType.SPARKLES)
            }
            else -> {
                lottieHelper.getAnimationView(requireContext().applicationContext,
                        LottieHelper.AnimationType.ERROR)
            }
        }
    }

    private fun undoLastLetter() {
        val erasedLetter = gameManager!!.subtractLetterFromAttempt()
        recreateErasedLetter(erasedLetter)
        if (wordContainer!!.childCount > 0) {
            wordContainer!!.removeViewAt(wordContainer!!.childCount - 1)
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
            val isCorrect = gameManager!!.addTappedLetterToCurrentWordAttempt(letterString)
            val lav =
                    lottieHelper.getAnimationViewOnTopOfLetter(
                            getLetterTapAnimation(isCorrect),
                            (motionEvent.x - 7).roundToInt(),
                            (motionEvent.y + 7).roundToInt())

            frameLayout?.addView(lav, 300, 300)
            lav.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    lav.elevation = 0f
                }

                override fun onAnimationEnd(animation: Animator?) {
//                    frameLayout.removeViewInLayout(lav)
                    frameLayout?.removeView(lav)
                }
            })
            gameManager?.onWordAnswered()
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

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (REQUEST_KEY == requestKey) {
            category = result.getString(KEY_ID)
            startViewModelProcesses(category)
        }
    }

    private fun startViewModelProcesses(category: String) {
        arViewModel.getModelsFromRepositoryByCategory(category)
                .observe(viewLifecycleOwner, Observer {
                    processModelData(it)
                })
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

//    override fun setCurrentCategoryFromFragment(category: String) {
//        parentFragmentManager.setFragmentResult(
//                CategoryFragment.REQUEST_KEY,
//                bundleOf(CategoryFragment.KEY_ID to category)
//        )
//    }
}
package com.hyunki.aryoulearning2.ui.main.ar

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import com.airbnb.lottie.LottieAnimationView
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Trackable
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
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

import java.util.ArrayList
import java.util.HashMap
import java.util.Objects

import javax.inject.Inject

//TODO-figure out bounce/float animation
class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment(), GameCommandListener {
    private var gameManager: GameManager? = null
    private var listener: NavListener? = null

    @Inject
    internal var viewModelProviderFactory: ViewModelProviderFactory? = null

    @Inject
    internal var lottieHelper: LottieHelper? = null

    private var arViewModel: ArViewModel? = null

    private var arFragment: ArFragment? = null

    private var gestureDetector: GestureDetector? = null
    private val playBalloonPop: MediaPlayer? = null

    private var hasFinishedLoadingModels = false
    private var hasFinishedLoadingLetters = false
    private var hasPlacedGame = false
    private var placedAnimation: Boolean = false

    private var frameLayout: FrameLayout? = null

    private var wordContainer: LinearLayout? = null
    private var wordValidatorLayout: View? = null
    private var wordCardView: CardView? = null
    private val wordValidatorCv: CardView? = null
    private var wordValidator: TextView? = null
    private var validatorWord: TextView? = null
    private var validatorWrongWord: TextView? = null
    private var validatorWrongPrompt: TextView? = null
    private var validatorImage: ImageView? = null
    private var validatorBackgroundImage: ImageView? = null
    private var validatorOkButton: Button? = null
    private var undo: ImageButton? = null

    private var tapAnimation: LottieAnimationView? = null

    private var exitMenu: View? = null
    private var exit: ImageButton? = null
    private var exitYes: Button? = null
    private var exitNo: Button? = null

    private var fadeIn: ObjectAnimator? = null
    private var fadeOut: ObjectAnimator? = null

    private var modelMapList: List<HashMap<String, ModelRenderable>> = ArrayList()
    private var letterMap = HashMap<String, ModelRenderable>()

    private val textToSpeech: TextToSpeech

    private var base: Node? = null
    private var mainAnchor: Anchor? = null
    private var mainAnchorNode: AnchorNode? = null
    private var mainHit: HitResult? = null

    init {
        this.textToSpeech = pronunciationUtil.textToSpeech
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
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

        setUpARScene(arFragment!!)

        requestCameraPermission(activity, RC_PERMISSIONS)
        arViewModel = ViewModelProviders.of(this, viewModelProviderFactory).get(ArViewModel::class.java)

        arViewModel!!.modelLiveData.observe(viewLifecycleOwner, { models -> arViewModel!!.setListMapsOfFutureModels(models) })

        arViewModel!!.futureModelMapList.observe(viewLifecycleOwner, { hashMaps ->
            arViewModel!!.setModelRenderables(hashMaps)
            arViewModel!!.setMapOfFutureLetters(hashMaps)
        })

        arViewModel!!.futureLetterMap.observe(viewLifecycleOwner, { map -> arViewModel!!.setLetterRenderables(map) })

        arViewModel!!.modelMapList.observe(viewLifecycleOwner, { hashMaps ->
            modelMapList = hashMaps
            hasFinishedLoadingModels = true
        })

        arViewModel!!.letterMap.observe(viewLifecycleOwner, { returnMap ->
            letterMap = returnMap
            hasFinishedLoadingLetters = true
        })

        arViewModel!!.loadModels()
    }

    private fun getKeysFromModelMapList(mapList: List<HashMap<String, ModelRenderable>>): List<String> {
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
        val scene = arFragment.arSceneView
                .scene
        val frame = arFragment.arSceneView.arFrame

        setOnTouchListener(scene)
        setAddOnUpdateListener(scene, frame)
    }

    private fun setOnTouchListener(scene: Scene) {
        scene.setOnTouchListener { hitTestResult: HitTestResult, event: MotionEvent ->
            if (!hasPlacedGame) {
                return@scene.setOnTouchListener gestureDetector !!. onTouchEvent event
            }
            false
        }
    }

    private fun setAddOnUpdateListener(scene: Scene, frame: Frame?) {
        scene.addOnUpdateListener { frameTime ->

            if (frame == null) {
                return@scene.addOnUpdateListener
            }
            if (frame!!.camera.trackingState != TrackingState.TRACKING) {
                return@scene.addOnUpdateListener
            }
            if (!hasPlacedGame) {
                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (!placedAnimation && plane.trackingState == TrackingState.TRACKING) {
                        placedAnimation = true
                        tapAnimation = lottieHelper!!.getAnimationView(context, LottieHelper.AnimationType.TAP)
                        lottieHelper!!.addTapAnimationToScreen(tapAnimation!!, activity!!, frameLayout!!)
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
        wordValidator = wordValidatorLayout!!.findViewById(R.id.word_validator)
        validatorImage = wordValidatorLayout!!.findViewById(R.id.validator_imageView)
        validatorBackgroundImage = wordValidatorLayout!!.findViewById(R.id.correct_star_imageView)
        validatorWord = wordValidatorLayout!!.findViewById(R.id.validator_word)
        validatorWrongPrompt = wordValidatorLayout!!.findViewById(R.id.validator_incorrect_prompt)
        validatorWrongWord = wordValidatorLayout!!.findViewById(R.id.validator_wrong_word)
        validatorOkButton = wordValidatorLayout!!.findViewById(R.id.button_validator_ok)
        //        wordValidatorCv.setVisibility(View.INVISIBLE);
        exitMenu = layoutInflater.inflate(R.layout.exit_menu_card, frameLayout, false)
        exit = view.findViewById(R.id.exit_imageButton)
        exitYes = exitMenu!!.findViewById(R.id.exit_button_yes)
        exitNo = exitMenu!!.findViewById(R.id.exit_button_no)
        undo = view.findViewById(R.id.button_undo)
    }

    private fun setListeners() {
        exit!!.setOnClickListener { v -> frameLayout!!.addView(exitMenu) }
        exitYes!!.setOnClickListener { v -> listener!!.moveToListFragment() }
        exitNo!!.setOnClickListener { v -> frameLayout!!.removeView(exitMenu) }
        undo!!.setOnClickListener { v -> undoLastLetter() }
        //        undo.setOnClickListener(v -> recreateErasedLetter(eraseLastLetter(letters)));
    }

    //TODO - refactor animations to separate class
    //
    private fun setAnimations() {
        fadeIn = Animations.Normal.setCardFadeInAnimator(wordValidatorCv!!)

        fadeIn!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                frameLayout!!.addView(wordValidatorLayout)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                validatorOkButton!!.setOnClickListener { v ->
                    fadeOut!!.startDelay = 500
                    fadeOut!!.start()
                }
            }
        })

        fadeOut = Animations.Normal.setCardFadeOutAnimator(wordValidatorCv)
        fadeOut!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                frameLayout!!.removeView(wordValidatorLayout)
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

        val frame = arFragment!!.arSceneView.arFrame
        if (frame != null) {
            if (!hasPlacedGame && tryPlaceGame(tap, frame)) {
                hasPlacedGame = true
                frameLayout!!.removeView(tapAnimation)
            }
        }
    }

    private fun tryPlaceGame(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {

            mainHit = frame.hitTest(tap)[0]

            val trackable = mainHit!!.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(mainHit!!.hitPose)) {
                // Create the Anchor.
                if (trackable.getTrackingState() == TrackingState.TRACKING) {
                    mainAnchor = mainHit!!.createAnchor()
                }

                mainAnchorNode = AnchorNode(mainAnchor)
                mainAnchorNode!!.setParent(arFragment!!.arSceneView.scene)
                //                    Node gameSystem = createGame(modelMapList.get(0));
                gameManager = GameManager(getKeysFromModelMapList(arViewModel!!.modelMapList.value!!), this, listener)
                Log.d("arhostfrag", "tryPlaceGame: " + arViewModel!!.modelMapList.value!!.size)
                val modelKey = gameManager!!.currentWordAnswer

                wordCardView!!.visibility = View.VISIBLE

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
        mainAnchorNode!!.addChild(base!!)
        placeLetters(name)
    }

    private fun refreshModelResources() {
        mainAnchorNode!!.anchor!!.detach()
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
        wordContainer!!.addView(t)
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
        pronunciationUtil = null
        //        playBalloonPop.reset();
        //        playBalloonPop.release();
    }

    private fun placeLetters(word: String) {
        for (i in 0 until word.length) {
            placeSingleLetter(
                    Character.toString(word[i]))
        }
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode = ModelUtil.getLetter(base, letterMap[letter], arFragment!!)
        letterAnchorNode.children[0].setOnTapListener(getNodeOnTapListener(letter, letterAnchorNode))

        Log.d("arx", "tryPlaceGame: " + letterMap[letter]!!)
        connectAnchorToBase(letterAnchorNode)
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment!!.arSceneView.scene.addChild(anchorNode)
        base!!.addChild(anchorNode)
    }

    private fun recreateErasedLetter(letterToRecreate: String) {
        if (letterToRecreate != "") {
            placeSingleLetter(letterToRecreate)
        }
    }

    private fun getNodeOnTapListener(letterString: String, letterAnchorNode: AnchorNode): Node.OnTapListener {

        return { hitTestResult, motionEvent ->

            gameManager!!.addLetterToAttempt(letterString)

            lottieHelper!!.addAnimationViewOnTopOfLetter(
                    getLetterTapAnimation(
                            checkIfTappedLetterIsCorrect(letterString)),
                    Math.round(motionEvent.getX() - 7),
                    Math.round(motionEvent.getY() + 7),
                    frameLayout!!)

            addLetterToWordBox(letterString.toLowerCase())
            gameManager!!.addTappedLetterToCurrentWordAttempt(letterString.toLowerCase())
            Objects.requireNonNull<Anchor>(letterAnchorNode.anchor).detach()
        }
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter = Character.toString(
                gameManager!!.currentWordAnswer[gameManager!!.attempt.length - 1])

        return if (tappedLetter.toLowerCase() == correctLetter.toLowerCase()) {
            true
        } else {
            false
        }
    }

    private fun getLetterTapAnimation(isCorrect: Boolean): LottieAnimationView {
        val lav: LottieAnimationView
        if (isCorrect) {
            lav = lottieHelper!!.getAnimationView(
                    activity, LottieHelper.AnimationType.SPARKLES)
        } else {
            lav = lottieHelper!!.getAnimationView(
                    activity, LottieHelper.AnimationType.ERROR)
        }
        return lav
    }

    override fun startNextGame(modelKey: String) {
        Log.d("startnextgame:arhostfragment", "startNextGame: condition hit")
        refreshModelResources()

        mainAnchor = mainHit!!.createAnchor()
        mainAnchorNode = AnchorNode(mainAnchor)
        mainAnchorNode!!.setParent(arFragment!!.arSceneView.scene)

        for (i in modelMapList.indices) {
            for ((key, value) in modelMapList[i]) {
                if (key == modelKey) {
                    createSingleGame(value, key)
                }
            }
        }
        wordContainer!!.removeAllViews()
    }

    fun addLetterToWordBox(letter: String) {
        addLetterToWordContainer(letter)
    }

    fun undoLastLetter() {
        val erasedLetter = gameManager!!.subtractLetterFromAttempt()
        recreateErasedLetter(erasedLetter)
        if (wordContainer!!.childCount > 0) {
            wordContainer!!.removeViewAt(wordContainer!!.childCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasPlacedGame = false
    }

    companion object {
        private val RC_PERMISSIONS = 0x123
        val MODEL_LIST = "MODEL_LIST"


        //    private void setValidatorCardView(boolean isCorrect) {
        //
        //        validatorWord.setText(currentWord);
        //        validatorWrongWord.setVisibility(View.INVISIBLE);
        //        validatorWrongPrompt.setVisibility(View.INVISIBLE);
        //
        //        if (isCorrect) {
        //            validatorBackgroundImage.setImageResource(R.drawable.star);
        //            Picasso.get().load(categoryList.get(roundCounter - 1).getImage()).into(validatorImage);
        //        } else {
        //            validatorBackgroundImage.setImageResource(R.drawable.error);
        //            Picasso.get().load(categoryList.get(roundCounter).getImage()).into(validatorImage);
        //            validatorWrongWord.setVisibility(View.VISIBLE);
        //            validatorWrongPrompt.setVisibility(View.VISIBLE);
        //        }
        //    }
        //
        //    private void compareAnswer(String letters, String word) {
        //        String validator = "";
        //        boolean isCorrect;
        //        if (letters.equals(word)) {
        //            isCorrect = true;
        //            validator = "correct";
        //
        //            //will run once when correct answer is entered. the method will instantiate, and add all from the current list
        //            categoryList.get(roundCounter).setWrongAnswerSet((ArrayList<String>) wrongAnswerList);
        //
        //            //we will increment once the list is added to correct index
        //            roundCounter++;
        //
        //            //this will remove all, seemed safer than clear, which nulls the object.
        //            wrongAnswerList.removeAll(wrongAnswerList);
        //
        //            pronunciationUtil.textToSpeechAnnouncer(validator, textToSpeech);
        //        } else {
        //            isCorrect = false;
        //            validator = "try again!";
        //            validatorWrongWord.setText(letters);
        //            correctAnswerSet.add(word);
        //            //every wrong answer, until a correct answer will be added here
        //            wrongAnswerList.add(letters);
        //            categoryList.get(roundCounter).setCorrect(false);
        //            pronunciationUtil.textToSpeechAnnouncer("incorrect, please try again", textToSpeech);
        //        }
        //
        //        wordValidator.setText(validator);
        //        setValidatorCardView(isCorrect);
        //        fadeIn.start();
        //
        //    }

        fun requestCameraPermission(activity: Activity?, requestCode: Int) {
            ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.CAMERA), requestCode)
        }
    }
}
package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import javax.inject.Inject
import com.hyunki.aryoulearning2.databinding.ActivityArfragmentHostBinding
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment() {
    val modelUtil = ModelUtil()

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    // TODO: animations
    //    @Inject
    //    lateinit var lottieHelper: LottieHelper

    @Inject
    lateinit var application: Application

    // ViewModels
    private val mainViewModel: MainViewModel by activityViewModels { viewModelProviderFactory }
    private lateinit var arViewModel: ArViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var coordinator: ArGameCoordinatorViewModel

    // AR
    private lateinit var frameLayout: FrameLayout
    private lateinit var arFragment: ArGameFragment
    private lateinit var base: Node
    private lateinit var mainHit: HitResult
    private lateinit var mainAnchor: Anchor
    private lateinit var mainAnchorNode: AnchorNode

    // Navigation
    private lateinit var listener: NavListener

    // ViewBinding
    private var _binding: ActivityArfragmentHostBinding? = null
    private val binding get() = _binding!!

    // TODO: navigation to replay fragment

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityArfragmentHostBinding.inflate(inflater, container, false)
        arFragment =
            childFragmentManager.findFragmentById(R.id.ux_fragment).let { it as ArGameFragment }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arViewModel = ViewModelProvider(this, viewModelProviderFactory).get(ArViewModel::class.java)
        gameViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(GameViewModel::class.java)
        coordinator = ViewModelProvider(
            this,
            ArGameCoordinatorFactory(arViewModel, gameViewModel, mainViewModel)
        ).get(
            ArGameCoordinatorViewModel::class.java
        )

        frameLayout = binding.frameLayout
        checkAndOpenCamera()
        initViews()
        setListeners()
        setOnTouchListener(arFragment)
//        runArPipeline()
        coordinator.uiState.observe(viewLifecycleOwner) { ui ->
            binding.wordContainerText.text = ui.attemptText
            binding.buttonUndo.isVisible = ui.isUndoVisible
            binding.cardWordContainer.isVisible = ui.isWordCardVisible

            ui.showResult?.let { isCorrect ->
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(250L)
                    showCard(isCorrect)
                    coordinator.consumeResult()
                }
            }

            Log.d("hyunki asdf coordinator state", ui.toString())

            if (ui.shouldStartNewRound &&
                ui.nextRenderable != null &&
                ui.nextModelKey != null
            ) {
                createSingleGame(ui.nextRenderable, ui.nextModelKey)
                coordinator.consumeRound()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO implement audio effects shutdown
    override fun onDestroy() {
        super.onDestroy()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {

            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndOpenCamera() {
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setOnTouchListener(arFragment: ArFragment) {
        arFragment.setOnTapArPlaneListener { _, _, motionEvent ->
            onSingleTap(motionEvent)
        }
    }

    private fun getListenerToAddLetterToAttempt(
        letterString: String,
        letterAnchorNode: AnchorNode
    ): Node.OnTapListener {
        return Node.OnTapListener { _, motionEvent ->
            gameViewModel.addLetterToAttempt(letterString)
            letterAnchorNode.anchor?.detach()
// TODO:animations
//            val lav =
//                lottieHelper.getAnimationViewOnTopOfLetter(
//                    getLetterTapAnimation(isCorrect),
//                    (motionEvent.x - 7).roundToInt(),
//                    (motionEvent.y + 7).roundToInt()
//                )
//            frameLayout.addView(lav, 300, 300)
//            lav.addAnimatorListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    frameLayout.removeView(lav)
//                }
//            })
        }
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment.arSceneView.scene.addChild(anchorNode)
        base.addChild(anchorNode)
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode =
            modelUtil.getLetter(base, gameViewModel.letterMap?.get(letter), arFragment)
        letterAnchorNode.children[0].setOnTapListener(
            getListenerToAddLetterToAttempt(
                letter,
                letterAnchorNode
            )
        )
        connectAnchorToBase(letterAnchorNode)
    }

    private fun undoLetter() {
        val letterToRecreate = gameViewModel.removeLetterFromAttempt()
        if (letterToRecreate != null) {
            placeSingleLetter(letterToRecreate)
        }
    }

    private fun placeLetters(word: String) {
        for (letter in word) {
            placeSingleLetter(
                letter.toString()
            )
        }
    }

    private fun createSingleGame(mainModel: ModelRenderable, name: String) {
        if (this::base.isInitialized) {
            mainAnchorNode.removeChild(base)
        }
        base = modelUtil.getGameAnchor(mainModel)
        mainAnchorNode.addChild(base)
        placeLetters(name)
    }

    fun checkHit(hit: HitResult?): Boolean {
        val trackable = hit?.trackable
        val pose = hit?.hitPose

        if (trackable is Plane && trackable.isPoseInPolygon(pose)) {
            // Create the Anchor.
            if (trackable.trackingState == TrackingState.TRACKING) {
                mainAnchor = mainHit.createAnchor()
            }

            mainAnchorNode = AnchorNode(mainAnchor)
            mainAnchorNode.setParent(arFragment.arSceneView.scene)

            val modelKey = gameViewModel.currentWord?.answer
            val entry = gameViewModel.getModelEntryFromModelKey(modelKey ?: "")

            if (entry != null) {
                gameViewModel.setHasPlacedGame(true)
            }
            return true
        }
        return false
    }

    fun setHitAndTryPlaceGame(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            mainHit = (frame.hitTest(tap)[0])
            return checkHit(mainHit)
        }
        return false
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!arViewModel.isLettersLoaded() ||
            !arViewModel.isModelsLoaded() ||
            gameViewModel.keyStack.isEmpty()
        ) {
            // Still loading assets or no words available yet
            return
        }
        if (gameViewModel.hasPlacedGame.value != true) {
            val frame = arFragment.arSceneView.arFrame
            if (frame != null) {
                if (setHitAndTryPlaceGame(tap, frame)) {
                    gameViewModel.setHasPlacedGame(true)
                }
            }
        }
    }

// TODO: check if necessary
//    private fun refreshModelResources() {
//        mainAnchorNode?.anchor?.detach()
//        mainAnchor = null
//        mainAnchorNode = null
//    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            (requireActivity() as MainActivity).showProgressBar(true)
        } else {
            (requireActivity() as MainActivity).showProgressBar(false)
        }
    }

    private fun setUpCardWithCorrectValidators() {
        //setup views to validate that the user is correct
        binding.validatorCard.correctStarImageView.setImageResource(R.drawable.star)
    }

    private fun setUpCardWithIncorrectValidators() {
        //setup views to inform the user they were incorrect
        binding.validatorCard.correctStarImageView.setImageResource(R.drawable.error)
        binding.validatorCard.validatorWrongWord.visibility = View.VISIBLE
        binding.validatorCard.validatorIncorrectPrompt.visibility = View.VISIBLE
    }

    private fun showCard(isCorrect: Boolean) {
        binding.validatorCard.validatorWord.text = gameViewModel.currentWord?.answer ?: ""
        binding.validatorCard.validatorWrongWord.visibility = View.INVISIBLE
        binding.validatorCard.validatorIncorrectPrompt.visibility = View.INVISIBLE
        Picasso.get().load(
            gameViewModel.currentWord?.image
        ).into(
            binding.validatorCard.validatorImageView
        )

        when (isCorrect) {
            true -> setUpCardWithCorrectValidators()
            else -> setUpCardWithIncorrectValidators()
        }

        binding.validatorCard.root.visibility = View.VISIBLE

        binding.validatorCard.buttonValidatorOk.setOnClickListener {
            gameViewModel.setNextWord()
            binding.validatorCard.root.visibility = View.INVISIBLE

            // 👇 after consuming this round, check if we're done
            if (gameViewModel.keyStack.isEmpty()) {
                // use your existing NavListener / NavController
                listener.moveToListFragment()
                // or findNavController().navigate(R.id.action_arHost_to_replayFragment)
            }
// TODO: track data for correct/incorrect answers (word history)
//            onHidingCard(isCorrect)
// TODO: animations
//            fadeOut.startDelay = 500
//            fadeOut.start()
        }
// TODO: animations
//        fadeIn.start()
    }

    private fun initViews() {
        binding.exitLayout.root.visibility = View.INVISIBLE
        binding.validatorCard.root.visibility = View.INVISIBLE;
    }

    private fun setListeners() {
        binding.exitImageButton.setOnClickListener {
            showExitMenu()
        }
        binding.exitLayout.exitButtonYes.setOnClickListener { listener.moveToListFragment() }
        binding.exitLayout.exitButtonNo.setOnClickListener {
            binding.exitLayout.root.visibility = View.INVISIBLE
        }
        binding.buttonUndo.setOnClickListener {
            undoLetter()
        }
    }

    private fun showExitMenu() {
        binding.exitLayout.root.apply {
            visibility = View.VISIBLE
            bringToFront()
            isClickable = true
            isFocusable = true
        }
    }
}
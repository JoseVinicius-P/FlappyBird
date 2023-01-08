package com.flappybird.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	//Texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo, canoBaixo, canoTopo, gameOver;

	//Formas para colizão
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoTopo, retanguloCanoBaixo;

	//atributos de configurações
	private float larguraDispositivo, alturaDispositivo;
	private float variacao = 0;
	private float gravidade= 0;
	private float posicaoPassaroY = 0, posicaoPassaroX;
	private float posicaoCanoX;
	private float posicaoCanoY;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int recorde = 0;
	private boolean passouCano = false;
	private int statusJogo = 0;

	//Exibição de textos
	private BitmapFont textoPontuacao, textoReinicar, textoMelhorPontuacao;
	private GlyphLayout containerTextoPontuacao, containerReiniciar, containerMelhorPontuacao;

	//COnfiguração do Sons
	Sound somVoando, somColisao, somPontuacao;

	//Salvar pontuação
	Preferences preferences;

	//Objetos da camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {
		incializarObjetos();
		incializarTexturas();
	}

	@Override
	public void render () {

		//Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharObjetos();
		detectarColisoes();
	}

	private void incializarTexturas(){
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");

		gameOver = new Texture("game_over.png");
	}

	private void incializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoPassaroY = alturaDispositivo/2;
		posicaoPassaroX = larguraDispositivo/5;
		posicaoCanoX = larguraDispositivo;
		espacoEntreCanos = 300;

		//configuração de texto
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(7);
		//para centralizar texto
		containerTextoPontuacao = new GlyphLayout();

		textoReinicar = new BitmapFont();
		textoReinicar.setColor(Color.GREEN);
		textoReinicar.getData().setScale(3);
		//para centralizar texto
		containerReiniciar = new GlyphLayout();

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.YELLOW);
		textoMelhorPontuacao.getData().setScale(3);
		//para centralizar texto
		containerMelhorPontuacao = new GlyphLayout();

		//Formas Geometricas para colisões
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoTopo = new Rectangle();

		//Incializar sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//Configuração das preferencias
		preferences = Gdx.app.getPreferences("FlappyBird");
		recorde = preferences.getInteger("recorde", 0);

		//Configuração da Camera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	private void desenharObjetos(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		batch.draw(fundo, 0,0, larguraDispositivo, alturaDispositivo);
		batch.draw( passaros[(int) variacao], posicaoPassaroX, posicaoPassaroY);
		batch.draw(canoBaixo, posicaoCanoX, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoY);
		batch.draw(canoTopo, posicaoCanoX, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoY);

		containerTextoPontuacao.setText(textoPontuacao, String.valueOf(pontos));
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2 - containerTextoPontuacao.width/2, alturaDispositivo-containerTextoPontuacao.height);

		if (statusJogo == 2){
			batch.draw(gameOver, larguraDispositivo/2 - (float) gameOver.getWidth()/2, alturaDispositivo/2 + (float) gameOver.getHeight());
			//para centralizar o texto
			containerReiniciar.setText(textoReinicar, "Toque para reiniciar");
			textoReinicar.draw(batch, "Toque para reiniciar", larguraDispositivo/2 - containerReiniciar.width/2, alturaDispositivo/2);
			//para centralizar
			containerMelhorPontuacao.setText(textoMelhorPontuacao, "Seu recorde é "+recorde+" pontos");
			textoMelhorPontuacao.draw(batch, "Seu recorde é "+recorde+" pontos", larguraDispositivo/2 - containerMelhorPontuacao.width/2, containerReiniciar.height*4);
			//textoMelhorPontuacao.draw(batch, "Seu recorde é: 0 pontos")
		}

		batch.end();
	}

	private void verificarEstadoJogo(){

		//Estados Possiveis
		/*
		0 - Jogo Inicial, passaro parado
		1 - Em execução
		2 - Colidiu
		 */

		if(pontos > recorde){
			recorde = pontos;
			preferences.putInteger("recorde", recorde);
		}

		boolean isToqueTela = Gdx.input.justTouched();

		if(statusJogo == 0){

			baterAsas();

			//Evento de toque
			if(isToqueTela && posicaoPassaroY < alturaDispositivo - 200) {
				gravidade = -15;
				statusJogo = 1;
				somVoando.play();
			}

		}else if (statusJogo == 1){

			baterAsas();

			//Evento de toque
			if(isToqueTela && posicaoPassaroY < alturaDispositivo - 200) {
				gravidade = -15;
				somVoando.play();
			}

			//Movimentar o cano
			posicaoCanoX -= Gdx.graphics.getDeltaTime()*200;
			if(posicaoCanoX + canoBaixo.getWidth() < 0) {
				posicaoCanoX = larguraDispositivo;
				posicaoCanoY = random.nextInt(1000) - 500;
				passouCano = false;
			}

			//Aplica gravidade ao passaro
			if(posicaoPassaroY > 0 || gravidade < 0)
				posicaoPassaroY = posicaoPassaroY - gravidade;

			gravidade++;

			if(posicaoPassaroY < 0) {
				somColisao.play();
				statusJogo = 2;
			}

		}else if (statusJogo == 2){

			//Faz o passaro cair
			if(posicaoPassaroY > 0 || gravidade < 0){
				posicaoPassaroY = posicaoPassaroY - gravidade;
				posicaoPassaroX -= Gdx.graphics.getDeltaTime()*800;
			}

			gravidade++;

			if(isToqueTela){
				statusJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoPassaroY = alturaDispositivo/2;
				posicaoPassaroX = larguraDispositivo/5;
				posicaoCanoX = larguraDispositivo;
			}
		}


	}

	private void detectarColisoes() {

		circuloPassaro.set(larguraDispositivo/5+ (float) passaros[0].getWidth()/2, posicaoPassaroY + (float) passaros[0].getHeight()/2,  (float) passaros[0].getWidth()/2);
		retanguloCanoTopo.set(
				posicaoCanoX, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoY,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		retanguloCanoBaixo.set(
				posicaoCanoX, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoY,
				canoBaixo.getWidth(), canoTopo.getHeight()
		);

		if(Intersector.overlaps(circuloPassaro, retanguloCanoTopo)
				|| Intersector.overlaps(circuloPassaro, retanguloCanoBaixo)){
			if (statusJogo == 1){
				somColisao.play();
				statusJogo = 2;
			}
		}

	}

	private void validarPontos() {
		if(posicaoCanoX < larguraDispositivo/5 - passaros[0].getWidth()){
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
	}

	private void baterAsas(){
		variacao += Gdx.graphics.getDeltaTime() * 8;
		if (variacao > 3)
			variacao = 0;
	}
	
	@Override
	public void dispose () {

	}
}

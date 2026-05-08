# Lançador de Dados — Ponderada 1 (Módulo 10)

**Alunos:** Cecília Galvão e Pablo Azevedo

Este README é, de propósito, escrito como um diário de bordo. A gente
preferiu contar o caminho que percorreu (inclusive as paradas erradas)
em vez de só listar a solução final. Achamos que isso reflete melhor o
que de fato aprendemos na atividade.

---

## O que o professor pediu

Recortando o enunciado em tópicos, para a gente conseguir ir riscando
um a um:

- [x] Abrir e executar o projeto Android no Android Studio.
- [x] Localizar a função principal da interface.
- [x] Identificar o bug que faz o D6 não retornar valores entre 1 e 6.
- [x] Corrigir a lógica do D6.
- [x] Adicionar opções de seleção para D10, D20 e D100.
- [x] Garantir que cada dado gere apenas valores no seu intervalo válido.
- [x] Permitir escolher o tipo de dado pela interface.
- [x] Mostrar o resultado depois do clique no botão.
- [x] **[IR ALÉM]** Mostrar faces diferentes conforme o tipo de dado.

---

## 1. Onde a gente *achou* que estava o código

Sinceramente, a primeira meia hora foi perdida procurando coisa errada.

Como em projetos Android antigos a tela costuma vir descrita em XML, a
nossa primeira aposta foi olhar a pasta `app/src/main/res/`. Abrimos:

- `res/values/strings.xml` — só tinha o nome do app.
- `res/values/themes.xml` e `res/values/colors.xml` — só tema/cores.
- `res/drawable/...` — só os ícones do launcher.

Ficamos perdidos. Não tinha nenhum `activity_main.xml` com `Button` e
`TextView` como a gente esperava. A Cecília chegou a comentar
*"será que o XML foi deletado?"*, e o Pablo abriu o `AndroidManifest.xml`
achando que ia encontrar o layout referenciado lá — mas só achou a
declaração da `MainActivity`, sem layout XML nenhum.

## 2. Onde o código realmente estava

Voltamos para `MainActivity.kt`, em
`app/src/main/java/carvalho/zanini/ponderada1/MainActivity.kt`, e aí o
centavo caiu: o projeto usa **Jetpack Compose**, então a UI é descrita
em Kotlin com funções `@Composable`, e não em XML. A função principal
da interface é a `LancadorDeDadosApp()` — é ela que `setContent { ... }`
chama dentro do `onCreate`.

Foi a primeira lição importante: em projetos Compose, "onde fica a tela"
é dentro do próprio Kotlin, em uma função anotada com `@Composable`.

## 3. O bug do D6

Achada a função certa, o bug ficou óbvio. A linha original era:

```kotlin
val valorSorteado = when (dadoSelecionado) {
    "D6" -> Random.nextInt(6)
    else -> 0
}
```

Rodamos o app no emulador umas dez vezes e em quase metade das jogadas
saiu `0`. Aí caiu a ficha: a gente lembrou (depois de checar a doc do
Kotlin) que `Random.nextInt(n)` devolve um inteiro **de 0 até n-1**,
ou seja, 0..5. Nunca chegava no 6, e ainda por cima podia dar 0, que
em dado real não existe.

> **Mini-confusão honesta:** num primeiro momento o Pablo trocou para
> `Random.nextInt(7)` achando que resolvia. Resolveu o "nunca chegar
> no 6" mas continuou podendo sair 0. Só na segunda tentativa a gente
> mudou para a forma correta com **dois argumentos**:
> `Random.nextInt(1, 7)` — intervalo `[1, 7)`, ou seja, 1..6 de fato.

## 4. Estendendo para D10, D20 e D100 — e como geramos o range

Aqui foram dois lugares para mexer (e a gente esqueceu o segundo na
primeira passada):

1. A lista `dados`, que era `listOf("D6",)` — só um item, então o
   `forEach` que monta os `RadioButton` só pintava o D6 na tela.
2. O `when` dentro do `onClick` do botão, que precisava saber sortear
   nos novos intervalos.

Da primeira vez a gente só mexeu na lista. Os RadioButtons apareceram
direitinho, dava para selecionar D20, mas o resultado **sempre saía 0**
— porque o `when` caía no `else -> 0`. Voltamos e adicionamos os ramos
faltantes:

```kotlin
val valorSorteado = when (dadoSelecionado) {
    "D6"   -> Random.nextInt(1, 7)
    "D10"  -> Random.nextInt(1, 11)
    "D20"  -> Random.nextInt(1, 21)
    "D100" -> Random.nextInt(1, 101)
    else   -> 0
}
```

### 4.1. Como o range é "impresso" pelo `Random.nextInt`

Vale parar e olhar com calma como esses números aparecem, porque foi
exatamente onde a gente errou na primeira tentativa.

A função `Random.nextInt(from, until)` em Kotlin gera um inteiro num
intervalo **semiaberto** `[from, until)`. Em outras palavras:

- `from` **entra** no sorteio (limite inferior inclusivo).
- `until` **não entra** no sorteio (limite superior exclusivo).

Isso explica por que, para um dado de N faces, a chamada correta é
`Random.nextInt(1, N + 1)` — a gente *soma 1* no limite de cima
justamente para "compensar" o fato dele ser exclusivo. O `1` que está
do lado esquerdo é o menor valor que um dado pode mostrar (não tem
"face 0" em dado nenhum).

A tabela abaixo mostra como cada dado se traduz nessa fórmula:

| Dado | Faces possíveis | Chamada                | Intervalo `[from, until)` |
| ---- | --------------- | ---------------------- | ------------------------- |
| D6   | 1..6            | `Random.nextInt(1, 7)` | `[1, 7)`                  |
| D10  | 1..10           | `Random.nextInt(1, 11)`| `[1, 11)`                 |
| D20  | 1..20           | `Random.nextInt(1, 21)`| `[1, 21)`                 |
| D100 | 1..100          | `Random.nextInt(1,101)`| `[1, 101)`                |

> **Como a gente confirmou que estava certo.** Antes de aceitar o
> conserto, rodamos o app várias vezes para cada dado e fomos
> anotando manualmente os valores que apareciam, prestando atenção
> em três coisas: (1) o **mínimo** alguma hora ser 1, (2) o **máximo**
> alguma hora ser N, (3) **nunca** sair 0 nem N+1. Não é teste
> automatizado, mas para um sorteio simples bastou para a gente
> ter confiança.

Lição central: estado e UI andam separados em Compose. Adicionar a
opção visual (lista `dados`) não adiciona o comportamento — o `when`
em cima do `Random.nextInt` é o coração da lógica.

## 5. Estado em Compose (a parte que mais nos confundiu)

A gente passou um tempo tentando entender por que o `var resultado` não
podia ser uma variável Kotlin "normal". Tentamos, inclusive, declarar:

```kotlin
var resultado = "Clique no botão para lançar o dado" // ❌ não funciona
```

Compilava, mas o texto na tela **nunca atualizava** depois do clique.
Pesquisando, entendemos que uma função `@Composable` é re-executada
quando o estado muda, e o Compose só sabe que algo mudou se o valor
estiver em um `mutableStateOf` lido via `by remember { ... }`.
Por isso a forma certa é:

```kotlin
var resultado by remember { mutableStateOf("Clique no botão...") }
```

Foi a parte da atividade em que mais sentimos que estávamos aprendendo
algo novo de verdade, e não só consertando.

## 6. [IR ALÉM] — faces diferentes por tipo de dado

Essa parte foi a que mais teve idas e voltas. Vale contar as três
versões pelas quais a gente passou.

**Versão 1 — imagens de verdade (`res/drawable/`).** A primeira
ideia foi colocar PNGs, uma por face do D6, e símbolos para os
demais. Esbarramos em (1) não achar um conjunto de assets livres
com licença clara e estilo consistente para D6, D10, D20 e D100, e
(2) D100 com 100 imagens não fazia sentido. Abandonamos.

**Versão 2 — caracteres Unicode.** Recuamos para algo que cabia em
uma `Text`: ⚀ ⚁ ⚂ ⚃ ⚄ ⚅ para o D6 e os emojis 🔟 / 🎲 / 💯 para
os demais. Funcionava, mas tinha um cheiro de gambiarra: o "dado"
era na verdade só uma fonte. Compilamos, testamos e seguimos.

**Versão 3 — Canvas + `drawRect` / `drawPath`** *(versão final)*.
Quando a gente entendeu que o Compose tem um `Canvas` próprio, ficou
claro que dava pra desenhar os dados de verdade em vez de depender
do que a fonte do sistema oferece. Foi o momento em que a atividade
deixou de ser "consertar o `Random`" e virou exercício de **gráficos
2D em Compose**.

A regra que adotamos foi:

- **D6** — `drawRect` desenha um quadrado preenchido + um segundo
  `drawRect` com `style = Stroke(...)` para o contorno. Só ele usa
  `drawRect` mesmo, porque é o único cuja silhueta é um retângulo.
- **D10**, **D20** e **D100** — todos usam `drawPath`, cada um com
  uma forma geométrica distinta:
  - **D10**: losango/pipa de 4 vértices (silhueta clássica do d10).
  - **D20**: triângulo equilátero (uma face do icosaedro).
  - **D100**: decágono regular gerado em loop com `cos`/`sin`,
    para sugerir o aspecto quase-esférico do d100 (zocchihedron).

### 6.1. O retângulo do D6 com `drawRect`

Para o D6 a gente fez questão de chamar `drawRect` duas vezes — uma
para o preenchimento, outra para o contorno (com `style = Stroke`):

```kotlin
val lado   = minOf(w, h) * 0.85f
val origem = Offset((w - lado) / 2f, (h - lado) / 2f)

drawRect(
    color = corFundo,
    topLeft = origem,
    size = Size(lado, lado)
)
drawRect(
    color = corContorno,
    topLeft = origem,
    size = Size(lado, lado),
    style = Stroke(width = tracoLargura)
)
```

A `origem` é calculada para o quadrado ficar **centralizado** dentro
do `Canvas`, independente do tamanho que ele acabar tendo na tela.

### 6.2. As outras formas com `drawPath`

Cada `drawPath` parte de um `Path` construído à mão. A lógica é:
`moveTo` para o primeiro vértice, `lineTo` para os demais, e
`close()` no fim para fechar a figura. Os exemplos resumidos:

```kotlin
// D10 — losango / pipa
val path = Path().apply {
    moveTo(cx, cy - r)
    lineTo(cx + r * 0.7f, cy)
    lineTo(cx, cy + r)
    lineTo(cx - r * 0.7f, cy)
    close()
}
```

```kotlin
// D20 — triângulo equilátero
val path = Path().apply {
    moveTo(cx, cy - r)
    lineTo(cx + r * 0.866f, cy + r * 0.5f)  // 0.866 ≈ sin(60°)
    lineTo(cx - r * 0.866f, cy + r * 0.5f)
    close()
}
```

```kotlin
// D100 — decágono via cos/sin
val n = 10
val path = Path()
for (i in 0 until n) {
    val angulo = -PI / 2 + 2 * PI * i / n
    val x = cx + (r * cos(angulo)).toFloat()
    val y = cy + (r * sin(angulo)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
}
path.close()
```

Em todos os três casos a gente faz `drawPath(path, color = corFundo)`
para preencher e depois um segundo `drawPath` com `Stroke(...)`
para o contorno — mesma técnica do `drawRect` do D6.

### 6.3. Imprimindo o número *em cima* da forma

A forma sozinha não mostra o resultado — o número precisa aparecer
por cima dela. Em vez de usar `drawText` no Canvas (que exige
`TextMeasurer` e fica mais verboso), a gente colocou tudo dentro de
um `Box` com `contentAlignment = Alignment.Center`. O `Box` empilha
filhos no eixo Z na ordem em que são declarados, então o `Canvas`
fica embaixo e a `Text` em cima:

```kotlin
Box(
    modifier = Modifier.size(180.dp),
    contentAlignment = Alignment.Center
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // drawRect / drawPath conforme o tipo do dado
    }
    if (valor != null) {
        Text(
            text = valor.toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )
    }
}
```

Esse `if (valor != null)` é o que esconde o número antes do primeiro
lançamento (e também quando a pessoa troca de dado, porque a gente
zera `valorAtual` no `onClick` do `RadioButton`). Assim o usuário
nunca vê um "87" dentro da silhueta de um D6, por exemplo.

### 6.4. Tropeços honestos do Canvas

> **`path.close()` esquecido.** Na primeira tentativa do D20 a
> gente esqueceu o `close()` e o triângulo apareceu sem a base —
> três traços soltos, parecendo uma letra "V" deitada.
>
> **`drawText` direto no Canvas.** A primeira tentativa de imprimir
> o número foi via `drawText` dentro do próprio `DrawScope`, mas
> isso pede um `TextMeasurer` e centralização manual em pixels.
> Trocamos por `Box` + `Text` e o problema sumiu — a gente herdou
> o alinhamento "de graça" do layout do Compose.

## 7. O que tentamos e *não* conseguimos

Para registrar com sinceridade:

- **Animação no botão / "rolar" o dado.** Tentamos usar
  `animate*AsState` para fazer o número girar antes de parar no valor
  final. Funcionou parcialmente, mas atropelava o estado e às vezes o
  valor exibido não era o valor sorteado — desligamos a animação para
  não comprometer a corretude. Fica como próximo passo.
- **Trocar os RadioButtons por um `SegmentedButton`.** Achamos visualmente
  mais bonito, mas o componente exige uma versão de Material 3 mais
  recente do que a configurada no `build.gradle.kts`, e atualizar
  estourou outros warnings que a gente não quis abrir agora.
- **Histórico das últimas jogadas.** Pensamos em manter uma lista dos
  últimos N resultados em um `LazyColumn`. Conseguimos fazer compilar,
  mas o layout ficou apertado em telas pequenas e preferimos não
  entregar algo meia-boca.

## 8. Como rodar

1. Abrir a pasta do projeto no **Android Studio** (Hedgehog ou mais
   recente recomendado).
2. Esperar o Gradle sincronizar.
3. Subir um emulador (qualquer API 24+) ou plugar um device.
4. `Run` na `MainActivity`.
5. Selecionar o tipo de dado e tocar em **Lançar dado**. O resultado
   numérico aparece embaixo, junto com a face correspondente.

## 9. Critérios de conclusão — auto-avaliação

| Critério                                          | Status |
| ------------------------------------------------- | ------ |
| D6 gera apenas valores de 1 a 6                   | ✅     |
| D10 gera apenas valores de 1 a 10                 | ✅     |
| D20 gera apenas valores de 1 a 20                 | ✅     |
| D100 gera apenas valores de 1 a 100               | ✅     |
| Interface permite escolher o tipo de dado         | ✅     |
| Resultado é exibido após o clique no botão        | ✅     |
| [IR ALÉM] Faces diferentes por tipo de dado       | ✅     |

---

No fim, o bug de uma linha (`Random.nextInt(6)`) acabou nos ensinando
mais sobre Compose, estado, e leitura de documentação do que sobre
geração de aleatórios em si. Vai a entrega.

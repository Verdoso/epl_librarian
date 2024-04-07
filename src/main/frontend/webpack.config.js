var path = require('path')
var webpack = require('webpack')

const APP_PATH = path.resolve(__dirname, '../resources/static/librarian');
const { VueLoaderPlugin } = require('vue-loader')
const CompressionPlugin = require("compression-webpack-plugin");

const PATHS = {
  src: path.resolve(__dirname, 'src'),
  js: path.join(APP_PATH, 'js'),
  node_modules: path.resolve(__dirname, 'node_modules')
}

module.exports = {
  entry: {
    'main': path.join(PATHS.src, 'main.js')
  },
  plugins: [
    // Filter out the moment locales to reduce bundle size
    // Locales that should be included MUST be added to the project, otherwise they won't be available for use)
    // References: https://github.com/jmblog/how-to-optimize-momentjs-with-webpack
    new webpack.IgnorePlugin({
      resourceRegExp: /^\.\/locale$/,
      contextRegExp: /moment$/,
    }),
    new VueLoaderPlugin(),
  ],
  output: {
    path: path.resolve(APP_PATH, 'dist'),
    publicPath: '/librarian/dist/',
    filename: '[name]_build.js'
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader'
        ],
      },
      {
        test: /\.less$/,
        use: [
          'vue-style-loader',
          'css-loader',
          'less-loader'
        ]
      },
      {
        test: /\.scss$/,
        use: [
          'vue-style-loader',
          'css-loader',
          'sass-loader'
        ]
      },
      {
        test: /\.sass$/,
        use: [
          'vue-style-loader',
          'css-loader',
          {
            loader: 'sass-loader',
            options: {
              // indentedSyntax: true,
              // sass-loader version >= 8
              sassOptions: {
                indentedSyntax: true
              }
            }
          }
        ]
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
          }
          // other vue-loader options go here
        }
      },
      {
        test: /\.js$/,
        use: [{
            loader: 'babel-loader'
        }],
        exclude: /node_modules/
      },
      {
        test: /\.(png|jpg|gif|eot|woff2|woff|ttf|svg)$/,
        type: 'asset/resource',
        dependency: { not: ['url'] },
//        use: [{
//            loader: 'file-loader',
//            options: {
//              name: '[name].[ext]',
//              //name: '[name].[ext]?[hash]'
//            }
//        }],
      }
    ]
  },
  resolve: {
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      jquery: "jquery/src/jquery",
    },
    extensions: ['*', '.js', '.vue', '.json']
  },
  devServer: {
    historyApiFallback: true,
    noInfo: true,
    overlay: true
  },
  performance: {
    hints: false
  },
  devtool: 'eval-source-map'
}

if (process.env.NODE_ENV === 'development') {
  module.exports.output.publicPath = '/webpack'
  module.exports.devServer = {
      proxy: [{
          context: ['/librarian'],
          target: 'http://localhost:7070',
          ws: true,
          changeOrigin: true
        }]
  }
}

if (process.env.NODE_ENV !== 'development') {
  module.exports.devtool = 'source-map'
  module.exports.optimization = {minimize : true}
  // http://vue-loader.vuejs.org/en/workflow/production.html
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"production"'
      }
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true
    }),
    new CompressionPlugin({ include: /\.(js)$/i, deleteOriginalAssets: true }),
  ])
}
var path = require('path')
var webpack = require('webpack')

const APP_PATH = path.resolve(__dirname, '../resources/static/librarian');
const VueLoaderPlugin = require('vue-loader/lib/plugin')

const PATHS = {
  src: path.resolve(__dirname, 'src'),
  js: path.join(APP_PATH, 'js'),
  node_modules: path.resolve(__dirname, 'node_modules')
}

module.exports = {
  entry: [
    path.join(PATHS.src, 'main.js'),
  ],
  plugins: [
  new VueLoaderPlugin()
  ],
  output: {
    path: path.resolve(APP_PATH, 'dist'),
    publicPath: '/librarian/dist/',
    filename: 'build.js'
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
        loader: 'babel-loader',
        exclude: /node_modules/
      },
      {
        test: /\.(png|jpg|gif|eot|woff2|woff|ttf|svg)$/,
        loader: 'file-loader',
        options: {
          name: '[name].[ext]?[hash]'
        }
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
  devtool: '#eval-source-map'
}

if (process.env.NODE_ENV === 'development') {
  module.exports.output.publicPath = '/webpack'
  module.exports.devServer = {
      proxy: {
        '/librarian': {
          target: 'http://localhost:7070',
          ws: true,
          changeOrigin: true
        }
      }
    }
}

if (process.env.NODE_ENV === 'production') {
  module.exports.devtool = '#source-map'
  module.exports.optimization = {minimize : true}
  // http://vue-loader.vuejs.org/en/workflow/production.html
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"production"'
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      compress: {
        warnings: false
      }
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true
    })
  ])
}
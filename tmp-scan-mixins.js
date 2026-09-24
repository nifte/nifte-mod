const fs = require('fs')
const path = require('path')
const { execFileSync } = require('child_process')

const src = 'c:/Users/Michael/Dev/nifte-mod/src'
const client =
  'c:/Users/Michael/Dev/nifte-mod/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-clientOnly-46fe2ac065/26.3/minecraft-clientOnly-46fe2ac065-26.3.jar'
const common =
  'c:/Users/Michael/Dev/nifte-mod/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-common-46fe2ac065/26.3/minecraft-common-46fe2ac065-26.3.jar'
const cp = `${client}${path.delimiter}${common}`

function walk(dir, acc = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      walk(full, acc)
    } else if (entry.name.endsWith('Mixin.java')) {
      acc.push(full)
    }
  }
  return acc
}

const cache = new Map()
function javap(cls) {
  if (cache.has(cls)) {
    return cache.get(cls)
  }
  let out = null
  try {
    out = execFileSync('javap', ['-classpath', cp, '-p', cls], {
      encoding: 'utf8',
    })
  } catch {
    out = null
  }
  cache.set(cls, out)
  return out
}

for (const file of walk(src)) {
  const text = fs.readFileSync(file, 'utf8')
  const mixinMatch = text.match(
    /@Mixin\((?:value\s*=\s*)?([A-Za-z0-9_.]+)\.class/,
  )
  if (!mixinMatch) {
    continue
  }
  const simple = mixinMatch[1]
  const importMatch = text.match(
    new RegExp(`import ([\\w.]+)\\.${simple.replaceAll('.', '\\.')};`),
  )
  if (!importMatch) {
    console.log(`NO IMPORT ${path.basename(file)} ${simple}`)
    continue
  }
  const fqcn = `${importMatch[1]}.${simple}`
  const out = javap(fqcn)
  if (!out) {
    console.log(`JAVAP FAIL ${fqcn}`)
    continue
  }
  const methods = []
  for (const line of out.split(/\r?\n/)) {
    const methodMatch = line.match(
      /(?:public|protected|private).+? ([A-Za-z0-9_$]+)\(([^)]*)\)/,
    )
    if (methodMatch) {
      methods.push([methodMatch[1], methodMatch[2]])
    }
  }
  const byName = new Map()
  for (const [name, args] of methods) {
    if (!byName.has(name)) {
      byName.set(name, [])
    }
    byName.get(name).push(args)
  }
  const handlers = [
    ...text.matchAll(
      /@(?:Inject|ModifyVariable|Redirect|ModifyArg|ModifyArgs|WrapOperation)\(([\s\S]*?)\)\s*(?:private|public|protected)/g,
    ),
  ]
  for (const handler of handlers) {
    const annotation = handler[1]
    const methodMatch = annotation.match(/method\s*=\s*"([^"]+)"/)
    if (!methodMatch || methodMatch[1].includes('(')) {
      continue
    }
    const name = methodMatch[1]
    const signatures = byName.get(name)
    if (!signatures) {
      console.log(`MISSING ${path.basename(file)} ${fqcn}.${name}`)
      continue
    }
    const after = text.slice(handler.index)
    const paramsMatch = after.match(
      /\)\s*(?:private|public|protected)[\s\S]*?\(([^)]*)\)\s*\{/,
    )
    const params = paramsMatch ? paramsMatch[1] : ''
    const callbackArgs = params
      .split(',')
      .map((part) => part.trim())
      .filter((part) => part && !part.includes('CallbackInfo'))
    if (signatures.length !== 1) {
      console.log(
        `OVERLOAD ${path.basename(file)} ${fqcn}.${name} handlerArgs=${callbackArgs.length}`,
      )
      continue
    }
    const targetArgCount =
      signatures[0] === '' ? 0 : signatures[0].split(',').length
    if (targetArgCount !== callbackArgs.length) {
      console.log(
        `MISMATCH ${path.basename(file)} ${fqcn}.${name} target=${targetArgCount} handler=${callbackArgs.length} (${signatures[0]})`,
      )
    }
  }
}
